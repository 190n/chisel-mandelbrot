package mandelbrot

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.experimental.BundleLiterals._

/**
  * Bundle representing a complex number.
  *
  * @param width       total width of the real and imaginary parts
  * @param binaryPoint position of the binary point relative to the least significant bit (e.g. 0 is
  *                    an integer; 1 allows halves)
  */
class Complex(width: Int, binaryPoint: Int) extends Bundle {
	// real
	val re = FixedPoint(width.W, binaryPoint.BP)
	// imaginary
	val im = FixedPoint(width.W, binaryPoint.BP)
}

object Complex {
	def apply(width: Int, binaryPoint: Int, re: Double, im: Double) =
		(new Complex(width, binaryPoint)).Lit(
			_.re -> re.F(width.W, binaryPoint.BP),
			_.im -> im.F(width.W, binaryPoint.BP),
		)

	def apply(precision: Int) = new Complex(precision + 4, precision)
}

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
		io.out := DontCare
	}
}
