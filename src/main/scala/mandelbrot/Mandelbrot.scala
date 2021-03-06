package mandelbrot

import chisel3._
import chisel3.util._

case class MandelbrotParams(
	precision: Int,
	iters: Int,
	parallelism: Int = 1,
	cyclesPerTransfer: Int = 1,
	// the default is 2^(-precision) but default values can't reference other parameters
	var step: Double = Double.NaN,
) {
	// boundaries must be precise to at least 1/4
	require(precision >= 2)

	val xMin = -2.0f
	val xMax =  0.5f
	val yMin = -1.25f
	val yMax =  1.25f

	// specify default value
	if (step.isNaN) {
		step = 1.0 / (1 << precision)
	}

	require(step <= 0.25)
	require(step > 0.0)
	// must be 1/(a power of 2) (accounting for FP inaccuracy)
	val stepBitsFloat = -(Math.log(step) / Math.log(2.0))
	require(Math.abs(stepBitsFloat % 1.0) < 1e-10)
	val stepBits = Math.round(stepBitsFloat)
	require(stepBits <= precision)

	val rows = ((xMax - xMin) / step).toInt
	val cols = ((yMax - yMin) / step).toInt
	// process each row in an integer number of cycles
	require(cols % parallelism == 0)
	require((rows * cols) % cyclesPerTransfer == 0)
	require(cols % (cyclesPerTransfer / rows) == 0)
	val elementsPerTransfer = (rows * cols) / cyclesPerTransfer
}

class MandelbrotIO(p: MandelbrotParams) extends Bundle {
	val outBlock = Valid(Vec(p.elementsPerTransfer, Bool()))
	val debug = Output(new Bundle {
		val c = Complex(p.precision)
		val didLoopRow = Bool()
	})
}

class Mandelbrot(val p: MandelbrotParams) extends Module {
	val io = IO(new MandelbrotIO(p))
	val results = SyncReadMem(p.rows * p.cols, Bool())
	val iterators = Seq.fill(p.parallelism)(Module(new MandelbrotIter(p.precision, p.iters)))
	iterators.foreach { it =>
		it.io.c.valid := false.B
		it.io.c.bits := DontCare
	}

	// states
	val computing :: sending :: done :: Nil = Enum(3)
	val state = RegInit(computing)
	val c = RegInit(Complex(p.xMin.F(p.precision.BP), p.yMin.F(p.precision.BP)))
	// whether we will be done with the whole thing after the next batch of iterations finishes
	val willFinish = RegInit(false.B)
	val (sendCycle, doneSending) = Counter(state === sending, p.cyclesPerTransfer)

	io.outBlock.valid := state === sending
	io.outBlock.bits := DontCare

	io.debug.c := c
	io.debug.didLoopRow := false.B

	when(state === computing) {
		// when they're all ready
		when(iterators.map{ _.io.c.ready }.reduce{ _ && _ }) {
			for (i <- 0 until p.parallelism) {
				// calculate input to this iterator
				val c_iter = Wire(Complex(p.precision))
				c_iter.re := c.re + (p.step * i).F(p.precision.BP)
				// printf(p"${c.re.asUInt} ${c_iter.re.asUInt}\t")
				c_iter.im := c.im
				iterators(i).io.c.valid := true.B
				iterators(i).io.c.bits := c_iter
			}

			// prepare the point where our next iteration will start
			val new_re = c.re + (p.step * p.parallelism).F(p.precision.BP)
			c.re := new_re
			when(new_re >= p.xMax.F(p.precision.BP)) {
				val new_im = c.im + p.step.F(p.precision.BP)
				c.re := p.xMin.F(p.precision.BP)
				c.im := new_im
				when(new_im >= p.yMax.F(p.precision.BP)) {
					willFinish := true.B
				}
			}
		}

		// all done
		when(iterators.map{ _.io.out.valid }.reduce{ _ && _ }) {
			when(willFinish) {
				state := sending
			}

			// store the results
			val rowIndex = (
				(iterators(0).io.out.bits.c.im - p.yMin.F(p.precision.BP))
				>> (p.precision - p.stepBits)
			).asUInt
			val firstColIndex = (
				(iterators(0).io.out.bits.c.re - p.xMin.F(p.precision.BP))
				>> (p.precision - p.stepBits)
			).asUInt
			for (i <- 0 until p.parallelism) {
				val index = rowIndex * p.cols.U + firstColIndex + i.U
				results(index) := iterators(i).io.out.bits.result
			}
		}
	}.elsewhen(state === sending) {
		val elementsSoFar = sendCycle * p.elementsPerTransfer.U
		val row = elementsSoFar / p.cols.U
		val firstCol = elementsSoFar % p.cols.U
		for (i <- 0 until p.elementsPerTransfer) {
			val col = firstCol + i.U
			val index = row * p.cols.U + col
			io.outBlock.bits(i) := results(index)
		}

		when(doneSending) {
			state := done
		}
	}
}
