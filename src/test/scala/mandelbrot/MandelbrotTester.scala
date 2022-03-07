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

	def performIterTest(dut: MandelbrotIter, c: ComplexModel) = {
		dut.io.c.valid.poke(true.B)
		dut.io.c.bits.re.poke(c.re.F(dut.precision.BP))
		dut.io.c.bits.im.poke(c.im.F(dut.precision.BP))
		dut.io.c.ready.expect(true.B)
		dut.io.out.valid.expect(false.B)
		dut.clock.step()

		var z = new ComplexModel(0, 0)
		var didDiverge = false
		for (i <- 0 until dut.iters) {
			dut.io.c.ready.expect(false.B)
			dut.io.out.valid.expect(false.B)
			dut.clock.step()
			z = ComplexModel.f(c, z)
			// println(f"[model] (${z.re}) + (${z.im})i")
			if (z.abs > 2.0) {
				didDiverge = true
				// println("[model] diverged")
			}
		}

		dut.io.out.valid.expect(true.B)
		dut.io.out.bits.expect(didDiverge.B)
		dut.clock.step()
		dut.io.out.valid.expect(false.B)
		dut.io.c.ready.expect(true.B)
	}

	behavior of "MandelbrotIter"
	it should "detect whether an input diverges" in {
		test(new MandelbrotIter(28, 25)) { dut =>
			// doesn't diverge
			performIterTest(dut, ComplexModel(-0.3, 0.2))
			// diverges
			performIterTest(dut, ComplexModel(-1.5, 1.0))

			for (re <- -4 to 4) {
				for (im <- -4 to 4) {
					performIterTest(dut, ComplexModel(re / 2.0, im / 2.0))
				}
			}
		}
	}
}
