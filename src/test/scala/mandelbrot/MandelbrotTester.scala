package mandelbrot

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MandelbrotTester extends AnyFlatSpec with ChiselScalatestTester {
	behavior of "MandelbrotFn"
	it should "calculate z^2 + c correctly" in {
		test(new MandelbrotFn(16)) { dut =>
			dut.io.enable.poke(true.B)
			dut.io.z.poke(Complex(20, 16, 0.375, 0.46875))
			dut.io.c.poke(Complex(20, 16, -0.875, 0.1875))
			dut.io.out.expect(Complex(20, 16, -0.9541015625, 0.5390625))

			dut.io.z.poke(Complex(20, 16, 0.125, -0.1875))
			dut.io.c.poke(Complex(20, 16, 0.3125, 0.75))
			dut.io.out.expect(Complex(20, 16, 0.29296875, 0.703125))
		}
	}
}
