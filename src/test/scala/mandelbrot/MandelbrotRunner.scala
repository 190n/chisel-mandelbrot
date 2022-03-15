package mandelbrot

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import scala.collection.mutable.ArrayBuffer

class MandelbrotRunner extends AnyFlatSpec with ChiselScalatestTester {
	behavior of "Mandelbrot"
	it should "run with arguments" in {
		val p = new MandelbrotParams(3, 30, 5, 100)
		test(new Mandelbrot(p)) { dut =>
			val cyclesPerCell = dut.p.iters + 2
			val numComputeBlocks = (dut.p.rows * dut.p.cols) / dut.p.parallelism
			dut.clock.setTimeout(cyclesPerCell * numComputeBlocks + dut.p.cyclesPerTransfer + 1)

			val grid = ArrayBuffer.fill(p.rows)(ArrayBuffer.fill(p.cols)(false))
			while (!dut.io.outBlock.valid.peekBoolean()) {
				dut.clock.step()
			}

			// A row (split into its blocks) should be
			// [-2.0, -1.5) [-1.5, -1.0) [-1.0, -0.5) [-0.5, 0.0) [0.0, 0.5)
			// but it is actually
			// [0.0, 0.5) [-2.0, -1.5) [-1.5, -1.0) [-1.0, -0.5) [-0.5, 0.0)
			def fixStartIndex(i: Int) = {
				if (i < p.elementsPerTransfer) {
					i + p.cols - p.elementsPerTransfer
				} else {
					i - p.elementsPerTransfer
				}
			}

			for (r <- 0 until p.rows) {
				for (c <- 0 until p.cols by p.elementsPerTransfer) {
					dut.io.outBlock.bits.zipWithIndex.foreach{ case (b, i) =>
						grid(r)(fixStartIndex(c) + i) = b.peekBoolean()
					}
					dut.clock.step()
				}

				grid(r).foreach(b => print(if (b) "  " else "##"))
				print("\n")
			}
		}
	}
}
