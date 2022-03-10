package mandelbrot

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MandelbrotIterTester extends AnyFlatSpec with ChiselScalatestTester {
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
