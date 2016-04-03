package serial;

import java.util.Set;

public abstract class Document {

	public static void register(Class<?> documentClass, Class<?>... itemClasses){
		DocumentMeta.register(documentClass, itemClasses);
	}
	public static Document newDocument(String className, String id) throws Exception {
		return DocumentMeta.newDocument(className, id, null);
	}
	public static Document newDocument(String className, String id, String jsonAll) throws Exception {
		return DocumentMeta.newDocument(className, id, jsonAll);
	}

	transient DocumentMeta _meta;
	public Document.Item getItem(String className, String id) throws Exception {
		return _meta.getItem(className, id);
	}
	public Set<String> getIds(String className) {
		return _meta.getIds(className);
	}
	public String serialize(int vmin){
		return _meta.serialize(vmin);
	}
	public int version() { return _meta.version(); }
	public String id() { return _meta.id(); }

	public static abstract class Item {
		transient DocumentMeta.ItemMeta _meta;
		public void save(){	_meta.save(); }
		public boolean modified() { return _meta.modified(); }
		public int version() { return _meta.version(); }
		public String id() { return _meta.id(); }

	}
}
