package serial;

import java.util.List;

public class ArrayDouble extends ArrayObject<Double> {

	private static final long serialVersionUID = 1L;

	public ArrayDouble setColumn(Cell.CellNode value) {
		super.setW(value);
		return this;
	}

	public double getZ(int index) {
		Double l = super.get(index);
		return l == null ? 0 : l;
	}

	public void copy(List<Double> a) {
		if (a != null) {
			if (!a.containsAll(this) || !this.containsAll(a)) {
				this.clear();
				for (int i = 0; i < a.size(); i++)
					this.add(a.get(i));
			}
		}
	}

}
