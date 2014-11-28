package com.zeebo.beryllium

/**
 * User: eric
 * Date: 11/28/14
 */
class OperationalTransform implements Cloneable {

	public static final int TYPE_INSERT = 1
	public static final int TYPE_IDENTITY = 0
	public static final int TYPE_DELETE = -1

	/**
	 * Character to insert
	 */
	char c
	/**
	 * Position to insert
	 */
	int p

	/**
	 * The type of Operational Transform (insert or delete)
	 */
	int type

	/**
	 * OT site id
	 */
	int sid
	/**
	 * OT sv timestamp
	 */
	int[] sv

	def isConcurrentWith(OperationalTransform other) {
		(0..<sv.length).every {
			(sv[it] + (it == sid ? -1 : 0)) == (other.sv[it] + (it == other.sid ? -1 : 0))
		}
	}

	void setSv(def sv) {
		this.sv = sv as int[]
	}

	int[] getSv() {
		return sv.clone()
	}

	String toString() {
		"$type:$c[$p]: $sid $sv"
	}
}
