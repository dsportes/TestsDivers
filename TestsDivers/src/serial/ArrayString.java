package serial;

import java.util.List;

public class ArrayString extends ArrayObject<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArrayString setW(Cell.CellNode value) {
		super.setW(value);
		return this;
	}

	public String getZ(int index) {
		String l = super.get(index);
		return l == null ? "" : l;
	}

	public void copy(List<String> a) {
		if (a != null) {
			if (!a.containsAll(this) || !this.containsAll(a)) {
				this.clear();
				for (int i = 0; i < a.size(); i++)
					this.add(a.get(i));
			}
		}
	}

}
