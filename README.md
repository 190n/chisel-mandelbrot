chisel-mandelbrot
=================

Eventually, this will be a Chisel generator that renders the Mandelbrot set.

The Mandelbrot set is the set of complex numbers _c_ such that the following function, when iterated starting with _z = 0_ (i.e. _f(f(f(c)))_), does not diverge (reach a magnitude greater than 2.0):

<!-- low-budget LaTeX -->
<p align="center">
	<em>f<sub>c</sub>(z) = z<sup>2</sup> + c</em>
</p>

So far, I have written [a Chisel module](./src/main/scala/mandelbrot/MandelbrotFn.scala) implementing that function on fixed-point complex numbers, [a tester for it](./src/test/scala/mandelbrot/MandelbrotTester.scala), [a Scala model of complex numbers](./src/test/scala/mandelbrot/ComplexModel.scala) (currently supports addition, multiplication, and the Mandelbrot function), and [its tester](./src/test/scala/mandelbrot/ComplexModelTester.scala).

Things to be implemented:

- [x] Compute _f<sub>c</sub>(z)_ on complex numbers
- [ ] Iterate _f<sub>c</sub>(z)_ until it diverges
- [ ] Perform that iteration on a grid of numbers
- [ ] Output the results over multiple cycles (similarly to the matrix multiplication homework)
- [ ] (stretch goal) Allow the user to select a specific region and re-calculate the Mandelbrot set
- [ ] (stretch goal) Display the results in a fancy way, possibly with ASCII art in a terminal or even as a web app

## Usage

You can run the tests with `sbt test`.

* * *

Based on [freechipsproject/chisel-template](https://github.com/freechipsproject/chisel-template).
