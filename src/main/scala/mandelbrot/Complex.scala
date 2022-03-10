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

	override def toPrintable: Printable = {
		val signChar = Mux(im < 0.F(binaryPoint.BP), '-'.U, '+'.U)
		p"0x${Hexadecimal(re.asSInt)} ${Character(signChar)} 0x${Hexadecimal(im.abs.asSInt)}i (>> $binaryPoint)"
	}
}

object Complex {
	def apply(width: Int, binaryPoint: Int, re: Double, im: Double) =
		(new Complex(width, binaryPoint)).Lit(
			_.re -> re.F(width.W, binaryPoint.BP),
			_.im -> im.F(width.W, binaryPoint.BP),
		)

	def apply(precision: Int) = new Complex(precision + 4, precision)
}
