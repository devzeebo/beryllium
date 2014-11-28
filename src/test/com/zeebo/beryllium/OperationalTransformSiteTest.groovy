package com.zeebo.beryllium

/**
 * User: eric
 * Date: 11/28/14
 */
class OperationalTransformSiteTest extends GroovyTestCase {

	OperationalTransformNetwork network

	OperationalTransformSite site1
	OperationalTransformSite site2
	OperationalTransformSite site3

	StringBuffer s1b
	StringBuffer s2b
	StringBuffer s3b

	void setUp() {
		network = new OperationalTransformNetwork()
		site1 = new OperationalTransformSite()
		site2 = new OperationalTransformSite()
		site3 = new OperationalTransformSite()

		s1b = new StringBuffer()
		s2b = new StringBuffer()
		s3b = new StringBuffer()

		def listener = { StringBuffer buffer, OperationalTransform ot ->
			if (ot.type == OperationalTransform.TYPE_INSERT) {
				buffer.insert(ot.p, ot.c)
			}
			if (ot.type == OperationalTransform.TYPE_DELETE) {
				buffer.deleteCharAt(ot.p)
			}
		}

		site1.addListener listener.curry(s1b)
		site2.addListener listener.curry(s2b)
		site3.addListener listener.curry(s3b)

		network.linkSite(site1)
		network.linkSite(site2)
		network.linkSite(site3)
	}

	void testCreateOperationalTransform() {
		OperationalTransform ot = site1.createOperationalTransform(0, 'a', 0)

		assert ot.sid == site1.id
		assert ot.sv.size() == site1.sv.size()
	}

	void testMultipleInsert() {
		site1.createOperationalTransform(OperationalTransform.TYPE_INSERT, 'a', 0)
		site1.createOperationalTransform(OperationalTransform.TYPE_INSERT, 'b', 1)
		site1.createOperationalTransform(OperationalTransform.TYPE_INSERT, 'c', 2)

		println s1b
		println s2b
		println s3b

		site2.tryInvokeRemote()
		site3.tryInvokeRemote()

		assert s1b.toString() == s2b.toString()
		assert s2b.toString() == s3b.toString()
	}

	void testMultipleInsertOverNetwork() {
		site1.createOperationalTransform(OperationalTransform.TYPE_INSERT, 'a', 0)
		site2.createOperationalTransform(OperationalTransform.TYPE_INSERT, 'b', 0)
		site3.createOperationalTransform(OperationalTransform.TYPE_INSERT, 'c', 0)

		site1.tryInvokeRemote()
		site2.tryInvokeRemote()
		site3.tryInvokeRemote()

		println s1b
		println s2b
		println s3b

		assert s1b.toString() == s2b.toString()
		assert s2b.toString() == s3b.toString()
	}


}
