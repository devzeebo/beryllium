package com.zeebo.beryllium

/**
 * User: eric
 * Date: 11/28/14
 */
class OperationalTransformSite {

	OperationalTransformNetwork network

	int id

	def sv

	IDSequence h
	List<OperationalTransform> receiveQueue

	List<Closure> listeners

	OperationalTransformSite() {
		sv = []
		h = new IDSequence()
		receiveQueue = [] as LinkedList

		listeners = []
	}

	def addListener(Closure c) {
		listeners << c
	}

	def createOperationalTransform(int type, def c, int p) {
		OperationalTransform trans = new OperationalTransform(type: type, sid: id, c: c as char, p: p)

		execute(trans)

		sv[id]++
		trans.sv = sv

		trans = updateHL trans

		network?.broadcast(id, trans)

		return trans
	}

	def tryInvokeRemote() {
		while(invokeRemote());
	}

	def invokeRemote() {
		OperationalTransform o = receiveQueue.find { OperationalTransform ot ->
			ot.sv[ot.sid] == sv[ot.sid] + 1 &&
					[(0..<ot.sid).toArray(), ((ot.sid + 1)..<sv.size()).toArray()].flatten().every {
				ot.sv[it] <= sv[it]
			}
		}

		if (o) {
			receiveQueue.remove(o)
			o = updateHR(o)
			if (o.type != OperationalTransform.TYPE_IDENTITY) {
				execute(o)
			}
			sv[o.sid]++
		}

		return o != null
	}

	private def execute(OperationalTransform trans) {
		listeners.each { it(trans) }
	}

	def includeEffect(OperationalTransform o1, OperationalTransform o2) {
		o1 = o1.clone()
		o2 = o2.clone()

		if (o2.p < o1.p) {
			if (o2.type == OperationalTransform.TYPE_INSERT) {
				o1.p++
			} else {
				o1.p--
			}
		} else if (o2.p == o1.p) {
			if (o1.type == OperationalTransform.TYPE_DELETE && o2.type == OperationalTransform.TYPE_INSERT) {
				o1.p++
			} else if (o1.type == OperationalTransform.TYPE_INSERT && o2.type == OperationalTransform.TYPE_INSERT && o1.sid > o2.sid) {
				o1.p++
			} else if (o1.type == OperationalTransform.TYPE_DELETE && o2.type == OperationalTransform.TYPE_DELETE) {
				o1.type = OperationalTransform.TYPE_IDENTITY
			}
		}

		return o1
	}

	def excludeEffect(OperationalTransform o1, OperationalTransform o2) {
		o1 = o1.clone()
		o2 = o2.clone()

		if (o2.p < o1.p) {
			if (o2.type == OperationalTransform.TYPE_INSERT) {
				o1.p--
			} else {
				o1.p++
			}
		} else if (o2.p == o1.p) {
			if (o1.type == OperationalTransform.TYPE_DELETE && o2.type == OperationalTransform.TYPE_DELETE) {
				o1.p++
			} else if (o1.type == OperationalTransform.TYPE_DELETE && o2.type == OperationalTransform.TYPE_INSERT) {
				throw new Exception()
			}
		}

		return o1
	}

	def swapEffects(OperationalTransform o1, OperationalTransform o2) {
		o1 = excludeEffect(o1, o2)
		o2 = includeEffect(o2, o1)

		return [o1, o2]
	}

	def updateHL(OperationalTransform o) {
		OperationalTransform o_ = o.clone()

		for (int i = h.deletes.size() - 1; i >= 0; i--) {
			def t
			(o_, t) = swapEffects(o_, h.deletes[i])
			h.deletes[i] = t
		}
		if (o.type == OperationalTransform.TYPE_DELETE) {
			h.deletes << o
		} else if (o.type == OperationalTransform.TYPE_INSERT) {
			h.insertions << o_
		}

		return o_
	}

	def convert2IHC(OperationalTransform o, def sq) {
		def sqh = []
		def sqc = []

		sq.each {
			if (it.sv == o.sv) {
				sqc << it
			} else {
				for (int k = sqc.size() - 1; k >= 0; k--) {
					def t
					(it, t) = swapEffects(it, sqc[k])
					sqc[k] = t
				}
				sqh << it
			}
		}
		return [sqh, sqc].flatten()
	}

	def itsq(OperationalTransform o, def sequence) {
		OperationalTransform o_ = o.clone()
		for (int i = 0; i < sequence.size(); i++) {
			o_ = includeEffect(o_, sequence[i])
			if (o_.type == OperationalTransform.TYPE_IDENTITY) {
				break
			}
		}
		return o_
	}

	def updateHR(OperationalTransform o) {
		h.insertions = convert2IHC(o, h.insertions)

		OperationalTransform o__ = itsq(o, h.insertions.findAll { it.isConcurrentWith(o) })
		OperationalTransform o_ = itsq(o__, h.deletes)

		if (o_.type == OperationalTransform.TYPE_DELETE) {
			h.deletes << o_
		} else if (o_.type == OperationalTransform.TYPE_INSERT) {
			OperationalTransform ox = o__.clone()

			for (int k = 0; k < h.deletes.size(); k++) {
				OperationalTransform oy = ox.clone()
				ox = includeEffect(ox, h.deletes[k])
				h.deletes[k] = includeEffect(h.deletes[k], oy)
			}
			h.insertions << o__
		}

		return o_
	}
}
