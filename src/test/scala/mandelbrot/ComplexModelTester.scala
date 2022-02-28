package mandelbrot

import org.scalatest.flatspec.AnyFlatSpec

class ComplexModelTester extends AnyFlatSpec {

behavior of "ComplexModel"
	it should "add correctly" in {
		val result = ComplexModel(3, -2).add(ComplexModel(-2.75, 0.5))
		assert(result.re == 0.25)
		assert(result.im == -1.5)
	}

	it should "multiply correctly" in {
		val result = ComplexModel(3, -2).mul(ComplexModel(-2.75, 0.5))
		assert(result.re == -7.25)
		assert(result.im == 7.0)
	}

	it should "calculate z^2 + c correctly" in {
		val result = ComplexModel.f(ComplexModel(0.3125, 0.75), ComplexModel(0.125, -0.1875))
		assert(result.re == 0.29296875)
		assert(result.im == 0.703125)
	}
}
