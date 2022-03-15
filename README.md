chisel-mandelbrot
=================

This is a Chisel generator that renders the Mandelbrot set.

The Mandelbrot set is the set of complex numbers _c_ such that the following function, when iterated starting with _z = 0_ (i.e. _f(f(f(c)))_), does not diverge (reach a magnitude greater than 2.0):

<!-- low-budget LaTeX -->
<p align="center">
	<em>f<sub>c</sub>(z) = z<sup>2</sup> + c</em>
</p>

See [my presentation](./slides.pdf) for information on the modules I implemented for this project.

## Usage

You can run the all tests with `sbt test`. To run only the "runner," which uses the module to print ASCII art to the terminal, run `sbt 'testOnly **.MandelbrotRunner'`.

* * *

Based on [freechipsproject/chisel-template](https://github.com/freechipsproject/chisel-template).
