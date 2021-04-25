package chopra;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.MIPInfoCallback;

public class InfoCallback extends MIPInfoCallback {

	public int nCuts;
	
	@Override
	protected void main() throws IloException {
		// getNcuts(IloCplex.CutType.Benders)
		//+ getNcuts(IloCplex.CutType.BQP)
		//+ getNcuts(IloCplex.CutType.LocalCover)
		//+ getNcuts(IloCplex.CutType.LocalImplBd)
		//+ getNcuts(IloCplex.CutType.ObjDisj)
		//+ getNcuts(IloCplex.CutType.RLT)
		//+ getNcuts(IloCplex.CutType.Tighten)

		nCuts = getNcuts(IloCplex.CutType.CliqueCover)
		+ getNcuts(IloCplex.CutType.Cover)
		+ getNcuts(IloCplex.CutType.Disj)
		+ getNcuts(IloCplex.CutType.FlowCover)
		+ getNcuts(IloCplex.CutType.FlowPath)
		+ getNcuts(IloCplex.CutType.Frac)
		+ getNcuts(IloCplex.CutType.GUBCover)
		+ getNcuts(IloCplex.CutType.ImplBd)
		+ getNcuts(IloCplex.CutType.LiftProj)
		+ getNcuts(IloCplex.CutType.MCF)
		+ getNcuts(IloCplex.CutType.MIR)
		+ getNcuts(IloCplex.CutType.SolnPool)
		+ getNcuts(IloCplex.CutType.Table)
		+ getNcuts(IloCplex.CutType.User)
		+ getNcuts(IloCplex.CutType.ZeroHalf);
	}
}
