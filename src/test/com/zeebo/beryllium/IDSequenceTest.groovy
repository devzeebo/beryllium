package com.zeebo.beryllium

/**
 * User: eric
 * Date: 11/28/14
 */
class IDSequenceTest extends GroovyTestCase {

	IDSequence sequence
	def inserts
	def deletes

	void setUp() {
		sequence = new IDSequence()

		inserts = [
				new OperationalTransform(type: OperationalTransform.TYPE_INSERT),
				new OperationalTransform(type: OperationalTransform.TYPE_INSERT),
				new OperationalTransform(type: OperationalTransform.TYPE_INSERT),
				new OperationalTransform(type: OperationalTransform.TYPE_INSERT)
		]

		deletes = [
		        new OperationalTransform(type: OperationalTransform.TYPE_DELETE),
		        new OperationalTransform(type: OperationalTransform.TYPE_DELETE),
		        new OperationalTransform(type: OperationalTransform.TYPE_DELETE),
		]

		sequence.insertions.addAll inserts
		sequence.deletes.addAll deletes
	}

	void testIDSequenceIterator() {
		int count = 0

		Iterator i = sequence.iterator()

		while(i.hasNext()) {
			i.next()
			count++
		}

		assert count == inserts.size() + deletes.size()
	}
}
