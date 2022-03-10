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
}
