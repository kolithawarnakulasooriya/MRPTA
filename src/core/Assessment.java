package core;

public class Assessment {
	private TargetTask sourceTarget, destinationTarget;
	private double timeConstraint, energyconsumption, straightLineDistance;
	private boolean winBid;
	public TargetTask getSourceTarget() {
		return sourceTarget;
	}

	public TargetTask getDestinationTarget() {
		return destinationTarget;
	}

	public double getTimeConstraint() {
		return timeConstraint;
	}

	public double getEnergyconsumption() {
		return energyconsumption;
	}

	public double getStraightLineDistance() {
		return straightLineDistance;
	}

	public boolean isWinBid() {
		return winBid;
	}
	
	public Assessment(TargetTask sourceTarget, TargetTask destTarget, double timeConstraint, double energyconsumption, double straightLineDistance, boolean winBid) {
		super();
		this.sourceTarget = sourceTarget;
		this.destinationTarget = destTarget;
		this.timeConstraint = timeConstraint;
		this.winBid = winBid;
		this.energyconsumption = energyconsumption;
		this.straightLineDistance = straightLineDistance;
	}
	
	public boolean compare(Assessment a) {
		return timeConstraint > a.timeConstraint;
	}
	
	@Override
	public String toString() {
	    return new StringBuilder()
	        .append("("+this.sourceTarget.id)
	        .append("->"+this.destinationTarget.id)
	        .append(" t:"+this.timeConstraint)
	        .append(" w/l:"+this.winBid+")")
	        .toString();
	}
}
