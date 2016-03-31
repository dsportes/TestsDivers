package serial;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import serial.IW;

public abstract class Cell {
	public abstract CellDescr cellDescr();
	
	long version = 0;
	public long version() {	return version;};

	public String line;
	public String column;
	public String cellType;
	
	private HashSet<CellNode> writtenCellNodes = new HashSet<CellNode>();
	
	public void resetUpdates() {
		writtenCellNodes.clear();
	}
	
	Cell init(String line, String column, long version, byte[] bytes) throws Exception {
		this.cellType = cellDescr().getName();
		this.line = line;
		this.column = column;
		this.version = version;
		cellDescr().read(this, bytes);
		// this.compile();
		resetUpdates();
		return this;
	}

	private TreeMap<String, CellNode> tree = new TreeMap<String, CellNode>();
	public TreeMap<String, CellNode> tree() { return tree; }

	public CellNode newCellNode(String nodeType) throws Exception {
		return cellDescr().newCellNode(this, nodeType);
	}

	public Set<CellNode> allNodes() {
		HashSet<CellNode> allNodes = new HashSet<CellNode>();
		Set<Map.Entry<String, CellNode>> s = tree.entrySet();
		for (Map.Entry<String, CellNode> me : s)
			allNodes.add(me.getValue());
		return allNodes;
	}

	public CellNode nodeByKey(String key) {
		String k = key != null ? key : "";
		return tree.get(k);
	}

	public List<CellNode> nodesByKey(String key) {
		ArrayList<CellNode> lst = new ArrayList<CellNode>();
		for (CellNode o : nodes(key)) {
			lst.add(o);
		}
		return lst;
	}

	public Collection<CellNode> nodes(String key) {
		String k = key != null ? key : "";
		return tree().subMap(k + '\u0000', k + '\uFFFF').values();
	}

	public Collection<CellNode> nodes() {
		return tree().values();
	}

	/**************************************************************************/
	
	public abstract class CellNode implements IW {
		private long version = 0;
		public long version() {	return version;};

		public void w(){
			writtenCellNodes.add(this);
		}
		
		public CellNode insert() {
			cellDescr().populateArrays(this);
			String singleKey = cellDescr().singleKey(this.getClass());
			if (singleKey != null) {
				tree.put(singleKey, this);
			} else {
				String[] keys = keys();
				if (keys != null && keys.length != 0) for (String k : keys)
					if (k != null) tree.put(k, this);
			}
			w();
			return this;
		}

		public CellNode remove() {
			String singleKey = cellDescr().singleKey(this.getClass());
			if (singleKey != null) {
				tree.remove(singleKey);
			} else {
				String[] keys = keys();
				if (keys != null && keys.length != 0) for (String k : keys)
					if (k != null) tree.remove(k);
			}
			w();
			return this;
		}

		public abstract String[] keys();
		
		public boolean w(String field, String value) {
			boolean b1 = (field == null || field.length() == 0);
			boolean b2 = (value == null || value.length() == 0);
			if ((b1 && b2) || (!b1 && !b2 && field.equals(value))) return false;
			w();
			return true;
		}

		public boolean w(int field, int value) {
			if (field == value) return false;
			w();
			return true;
		}

		public boolean w(long field, long value) {
			if (field == value) return false;
			w();
			return true;
		}

		public boolean w(double field, double value) {
			if (field == value) return false;
			w();
			return true;
		}
		
		/*
		 * Retourne la liste des numéro de champs à sortir en JSON<br>
		 * Par convention, null les retourne TOUS<br>
		 * Par convention int[0] (array vide) SAUTE le cellNode
		 */
		public int[] jsonFields(String filterArg) { return null; }

	}
	
}
