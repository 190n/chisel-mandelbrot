package mandelbrot

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MandelbrotTester extends AnyFlatSpec with ChiselScalatestTester {
	behavior of "MandelbrotParams"
	it should "calculate parameters correctly" in {
		// expect failure
		// precision 4 = 40x40
		intercept[Exception] {
			// 20 cycles not ok since we would send 2 rows at once
			val p = new MandelbrotParams(4, 50, 1, 20)
		}
		intercept[Exception] {
			// 30 cycles doesn't divide the number of cells
			val p = new MandelbrotParams(4, 50, 1, 30)
		}
		intercept[Exception] {
			// cycles is okay, parallelism must divide number of columns
			var p = new MandelbrotParams(4, 50, 3, 160)
		}

		val p = new MandelbrotParams(4, 50, 1, 160)
		assert(p.rows == 40)
		assert(p.cols == 40)
		assert(p.step == 0.0625)
		assert(p.elementsPerTransfer == 10)
	}

	def doMandelbrotTest(dut: Mandelbrot) = {
		// 2 extra cycles for input and output
		val cyclesPerCell = dut.p.iters + 2
		val numComputeBlocks = (dut.p.rows * dut.p.cols) / dut.p.parallelism
		dut.clock.setTimeout(cyclesPerCell * numComputeBlocks + dut.p.cyclesPerTransfer + 1)

		var lastRe = -99999.0
		for (i <- 0 until cyclesPerCell * numComputeBlocks) {
			dut.io.outBlock.valid.expect(false.B)
			if (dut.io.debug.didLoopRow.peekBoolean()) {
				println(dut.io.debug.c.re.peekDouble())
			}
			dut.clock.step()
		}

		for (r <- 0 until dut.p.rows) {
			for (c <- 0 until dut.p.cols by dut.p.elementsPerTransfer) {
				dut.io.outBlock.valid.expect(true.B)
				// IN the set (shaded) <=> did NOT diverge
				dut.io.outBlock.bits.foreach{ b => print(if (b.peekBoolean()) " " else "#") }
				dut.clock.step()
			}
			print("$\n")
		}

		dut.io.outBlock.valid.expect(false.B)
	}

	behavior of "Mandelbrot"
	it should "just show me the output" in {
		// precision 3 = 20x20
		val p = new MandelbrotParams(3, 30, 5, 80)
		test(new Mandelbrot(p)) { dut =>
			doMandelbrotTest(dut)
		}
	}
}
