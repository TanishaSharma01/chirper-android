import reactions.IReactionReporter;
import reactions.ReactionReportFactory;

public class ReportSources {
	/**
	 * Returns a list of the various implementations that will be used to run tests against
	 * for ReactionReportOverviewTests. By default, this uses just the implementation written
	 * in task 2, but you may add further implementations for testing.
	 * Your changes to this file will not be used when auto-marking, so please do not add methods
	 * or modify the signature of this method.
	 */
	public IReactionReporter[] getReporters() {
		return new IReactionReporter[] {ReactionReportFactory.buildReporter("overview")};
	}
}
