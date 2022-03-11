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

	behavior of "Mandelbrot"
	it should "just show me the output" in {
		// precision 2 = 10x10
		// expect 1000 cycles?
		val p = new MandelbrotParams(2, 10, 2, 20)
		assert(p.rows == 10)
		assert(p.cols == 10)
		test(new Mandelbrot(p)) { dut =>
			var cycles = 0
			while (dut.io.outBlock.valid.peekBoolean() == false) {
				cycles += 1
				dut.clock.step(1)
			}
			println(f"started transferring after $cycles cycles")
		}
	}
}
