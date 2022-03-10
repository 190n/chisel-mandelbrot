package mandelbrot

import chisel3._
import chisel3.util._

case class MandelbrotParams(
	precision: Int,
	iters: Int,
	parallelism: Int = 1,
	cyclesPerTransfer: Int = 1
) {
	// boundaries must be precise to at least 1/4
	require(precision > 2)

	val xMin = -2f
	val xMax = 0.5
	val yMin = -1.25
	val yMax = 1.25

	val rows = ((xMax - xMin) * (1 << precision)).toInt
	val cols = ((yMax - yMin) * (1 << precision)).toInt
	val step = 1.0 / (1 << precision)
	// process each row in an integer number of cycles
	require(cols % parallelism == 0)
	require((rows * cols) % cyclesPerTransfer == 0)
	require(cols % (cyclesPerTransfer / rows) == 0)
	val elementsPerTransfer = (rows * cols) / cyclesPerTransfer
}

class MandelbrotIO(p: MandelbrotParams) extends Bundle {
	val outBlock = Valid(Vec(p.elementsPerTransfer, Bool()))
}

class Mandelbrot(p: MandelbrotParams) extends Module {
	val io = IO(new MandelbrotIO(p))
	val results = Reg(Vec(p.rows, Vec(p.cols, Bool())))
	val iterators = Seq.fill(p.parallelism)(new MandelbrotIter(p.precision, p.iters))

	// states
	val ready :: computing :: sending :: Nil = Enum(3)
	val state = RegInit(ready)
	val c = Complex(p.xMin.F(p.precision.BP), p.yMin.F(p.precision.BP))
	c.im := c.im + 0.1.F(p.precision.BP)

	when(state === ready) {

	}.elsewhen(state === computing) {

	}.elsewhen(state === sending) {

	}
}
