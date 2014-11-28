package com.zeebo.beryllium

/**
 * User: eric
 * Date: 11/28/14
 */
class OperationalTransformNetwork {

	List<OperationalTransformSite> sites

	OperationalTransformNetwork() {
		sites = []
	}

	synchronized def linkSite(OperationalTransformSite site) {
		site.network = this
		site.id = sites.size()

		(0..<sites.size()).each {
			site.sv << 0
		}

		sites << site
		sites.each {
			it.sv << 0
		}
	}

	void broadcast(int sendingSite, OperationalTransform transform) {
		sites.each {
			if (it.id != sendingSite) {
				it.receiveQueue << transform
			}
		}
	}
}
