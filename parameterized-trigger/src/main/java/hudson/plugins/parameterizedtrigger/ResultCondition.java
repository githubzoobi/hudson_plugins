package hudson.plugins.parameterizedtrigger;

import hudson.model.Result;

public enum ResultCondition {

	SUCCESS("Stable") {
		boolean isMet(Result result) {
			return result == Result.SUCCESS;
		}
	},
    UNSTABLE("Unstable") {
                   boolean isMet(Result result) {
                           return result == Result.UNSTABLE;
                   }
           },
	UNSTABLE_OR_BETTER("Stable or unstable but not failed") {
		boolean isMet(Result result) {
			return result.isBetterOrEqualTo(Result.UNSTABLE);
		}
	},
	FAILED("Failed") {
		boolean isMet(Result result) {
			return result == Result.FAILURE;
		}
	};

	private ResultCondition(String displayName) {
		this.displayName = displayName;
	}

	private final String displayName;
	
	public String getDisplayName() {
		return displayName;
	}

	abstract boolean isMet(Result result);
	
}
