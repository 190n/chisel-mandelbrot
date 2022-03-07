package mandelbrot

import chisel3._
import chisel3.util._
import chisel3.experimental.FixedPoint

class MandelbrotIter(val precision: Int, val iters: Int) extends Module {
	val io = IO(new Bundle {
		val c   = Flipped(Decoupled(Complex(precision)))
		val out = Valid(Bool())
	})

	val z = Reg(Complex(precision))
	val c = Reg(Complex(precision))
	val counting = RegInit(false.B)
	val didDiverge = RegInit(false.B)
	val (iter, willWrap) = Counter(0 to iters, counting)
	val mfn = Module(new MandelbrotFn(precision))

	io.c.ready := !counting
	io.out.valid := false.B
	io.out.bits := DontCare
	mfn.io.z := z
	mfn.io.c := c
	mfn.io.enable := counting && !didDiverge

	when(io.c.fire) {
		c := io.c.bits
		z.re := 0.F(precision.BP)
		z.im := 0.F(precision.BP)
		counting := true.B
		didDiverge := false.B
	}.elsewhen(!willWrap && !didDiverge) {
		mfn.io.z := z
		mfn.io.c := c
		z := mfn.io.out
		printf(p"[hw] ${mfn.io.out}\n")
		// if |z| > 2.0
		val abs = z.re * z.re + z.im * z.im
		// compare with 4 since we didn't take square root
		when(abs > 4.F(precision.BP)) {
			didDiverge := true.B
			// printf(p"[hw] diverged\n")
		}
	}.elsewhen(willWrap) {
		counting := false.B
		io.out.valid := true.B
		io.out.bits := didDiverge
	}
}
