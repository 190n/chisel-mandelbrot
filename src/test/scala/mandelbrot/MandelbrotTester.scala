package mandelbrot

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MandelbrotTester extends AnyFlatSpec with ChiselScalatestTester {
	behavior of "MandelbrotFn"
	it should "calculate z^2 + c correctly" in {
		test(new MandelbrotFn(16, 8)) { dut =>
			dut.io.z.poke(Complex(16, 8, 3.0, -2.0))
			dut.io.c.poke(Complex(16, 8, 6.0, -8.0))
			dut.io.out.expect(Complex(16, 8, 11.0, -20.0))

			dut.io.z.poke(Complex(16, 8, 0.125, -0.1875))
			dut.io.c.poke(Complex(16, 8, 0.3125, 0.75))
			dut.io.out.expect(Complex(16, 8, 0.29296875, 0.703125))
		}
	}
}
