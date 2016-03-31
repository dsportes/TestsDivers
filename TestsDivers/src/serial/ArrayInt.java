package serial;

import java.util.List;

public class ArrayInt extends ArrayObject<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArrayInt setW(Cell.CellNode value) {
		super.setW(value);
		return this;
	}

	public int getZ(int index) {
		Integer l = super.get(index);
		return l == null ? 0 : l;
	}

	public void copy(List<Integer> a) {
		if (a != null) {
			if (!a.containsAll(this) || !this.containsAll(a)) {
				this.clear();
				for (int i = 0; i < a.size(); i++)
					this.add(a.get(i));
			}
		}
	}

}
