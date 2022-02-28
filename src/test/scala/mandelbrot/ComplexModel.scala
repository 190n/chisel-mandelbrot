package mandelbrot

case class ComplexModel(re: Double, im: Double) {
	def add(that: ComplexModel) = ComplexModel(
		this.re + that.re,
		this.im + that.im,
	)

	def mul(that: ComplexModel) = ComplexModel(
		this.re * that.re - this.im * that.im,
		this.re * that.im + this.im * that.re,
	)
}

object ComplexModel {
	def apply(re: Double, im: Double) = new ComplexModel(re, im)
	def f(c: ComplexModel, z: ComplexModel) = z.mul(z).add(c)
}
