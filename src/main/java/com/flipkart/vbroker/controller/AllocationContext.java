package com.flipkart.vbroker.controller;

/**
 * @author govind.ajith
 *
 */
public class AllocationContext {

	private AllocationStrategy allocationStrategy;

	/**
	 * @param allocationStrategy
	 */
	public void setAllocationStrategy(AllocationStrategy allocationStrategy) {
		this.allocationStrategy = allocationStrategy;
	}

	/**
	 * @return
	 */
	public AllocationStrategy getAllocationStrategy() {
		return this.allocationStrategy;
	}

	/**
	 * Perform allocation.
	 */
	public void allocate() {
		this.allocationStrategy.allocate();
	}

}
