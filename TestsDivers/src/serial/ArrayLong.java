package serial;

import java.util.List;

public class ArrayLong extends ArrayObject<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArrayLong setW(Cell.CellNode value) {
		super.setW(value);
		return this;
	}

	public long getZ(int index) {
		Long l = super.get(index);
		return l == null ? 0 : l;
	}

	public void copy(List<Long> a) {
		if (a != null) {
			if (!a.containsAll(this) || !this.containsAll(a)) {
				this.clear();
				for (int i = 0; i < a.size(); i++)
					this.add(a.get(i));
			}
		}
	}

}
