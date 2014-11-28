package com.zeebo.beryllium

/**
 * User: eric
 * Date: 11/28/14
 */
class OperationalTransformNetworkTest extends GroovyTestCase {

	OperationalTransformNetwork network
	OperationalTransformSite site1
	OperationalTransformSite site2
	OperationalTransformSite site3

	void setUp() {
		network = new OperationalTransformNetwork()

		site1 = new OperationalTransformSite()
		site2 = new OperationalTransformSite()
		site3 = new OperationalTransformSite()
	}

	void testLinkSite() {
		network.linkSite(site1)

		assert site1.sv.size() == 1

		network.linkSite(site2)
		network.linkSite(site3)

		[site1, site2, site3].each {
			assert it.sv.size() == 3
		}
	}
}
