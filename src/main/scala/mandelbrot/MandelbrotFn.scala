package mandelbrot

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.experimental.BundleLiterals._

/**
  * Compute z^2 + c on fixed-point imaginary numbers
  * 
  * @param precision number of bits after the binary point
  */
class MandelbrotFn(precision: Int) extends Module {
	val io = IO(new Bundle {
		val z      = Input(Complex(precision))
		val c      = Input(Complex(precision))
		val enable = Input(Bool())
		val out    = Output(Complex(precision))
	})

	when(io.enable) {
		// let z = AZ + BZi, c = AC + BCi
		// z^2 = (AZ + BZi)^2 = AZ^2 + (2AZBZ)i + (BZi)^2
		//                    = (AZ^2 - BZ^2) + (2AZBZ)i
		io.out.re := (io.z.re * io.z.re) - (io.z.im * io.z.im) + io.c.re
		io.out.im := ((io.z.re * io.z.im) << 1) + io.c.im
	}.otherwise {
		io.out.re := 0.F(precision.BP)
		io.out.im := 0.F(precision.BP)
	}
}
