package serial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public abstract class Document {
	public static final Logger log = Logger.getAnonymousLogger();	
	
	public static void register(Class<?> documentClass, Class<?>... itemClasses){
		String cn = documentClass.getSimpleName();
		DocumentDescriptor dd = DocumentDescriptor.documentDescriptor(cn);
		if (dd != null) return;
		dd = new DocumentDescriptor();
		dd.documentClassName = cn;
		DocumentDescriptor.ddList.add(dd);
		dd.documentClass = documentClass;
		dd.innerClasses = dd.documentClass.getDeclaredClasses();
		dd.innerClassNames = new String[dd.innerClasses.length];
		for(int i = 0; i < dd.innerClasses.length; i++)
			dd.innerClassNames[i] = dd.innerClasses[i].getSimpleName();
		for(Class<?> c : itemClasses)
			dd.itemDescriptor(c.getSimpleName());
	}
	
	@SuppressWarnings("serial")
	private static class ItemMap extends HashMap<String,Item> {}
	
	private static class DocumentDescriptor {
		private static ArrayList<DocumentDescriptor> ddList = new ArrayList<DocumentDescriptor>();
		
		private static DocumentDescriptor documentDescriptor(String className){
			for(DocumentDescriptor dd : ddList)
				if (dd.documentClassName.equals(className)) return dd;
			return null;
		}
		
		private class ItemDescriptor {
			private String itemClassName;
			private Class<?> itemClass;
			private int index;
		}
		
		private ItemDescriptor itemDescriptor(String className){
			for(ItemDescriptor id : idList)
				if (id.itemClassName.equals(className)) return id;
			ItemDescriptor id = new ItemDescriptor();
			id.itemClassName = className;
			for(int i = 0; i < innerClasses.length; i++)
				if (innerClassNames[i].equals(className))
					id.itemClass = innerClasses[i];
			if (id.itemClass == null)
				return null;
			id.index = idList.size();
			idList.add(id);
			return id;
		}

		private String documentClassName;
		private Class<?> documentClass;
		private Class<?>[] innerClasses;
		private String[] innerClassNames;
		private ArrayList<ItemDescriptor> idList = new ArrayList<ItemDescriptor>();
		private Gson gson = new Gson();
		
	}
	public static Document newDocument(String className, String id) throws Exception {
		return newDocument(className, id, null);
	}
	public static Document newDocument(String className, String id, String jsonAll) throws Exception {
		DocumentDescriptor dd = DocumentDescriptor.documentDescriptor(className);
		if (dd == null) return null;
		Document d = null;
		if (jsonAll == null || jsonAll.length() == 0) {
			d = (Document)dd.documentClass.newInstance();
			d._documentDescriptor = dd;
			synchronized (dd.gson) {
				d._json = dd.gson.toJson(d);
			}
			d._id = id;
			d._documentVersion = 0;
			d._items = new ItemMap[dd.idList.size()];
			return d;
		}
		int s = jsonAll.indexOf('\n');
		int vv = Integer.parseInt(jsonAll.substring(1, s));
		s = s + 2;
		int i = jsonAll.indexOf('\n', s);
		if ("null".equals(jsonAll.substring(s, i))) {
			d = (Document)dd.documentClass.newInstance();
			d._documentVersion = vv;
			d._documentDescriptor = dd;
			synchronized (dd.gson) {
				d._json = dd.gson.toJson(d);
			}
			d._id = id;
			d._items = new ItemMap[dd.idList.size()];
			d._version = -1;
		} else {
			int vx = jsonAll.lastIndexOf(',', i);
			String x = jsonAll.substring(s, vx) + "}";
			synchronized (dd.gson) {
				d = (Document)dd.gson.fromJson(x, dd.documentClass);
			}
			d._documentVersion = vv;
			d._version = Integer.parseInt(jsonAll.substring(vx + 6, i - 1));
			d._json = x;
		}
		d._id = id;
		d._documentDescriptor = dd;
		d._items = new ItemMap[dd.idList.size()];
		i = i + 4;
		while (true){
			int j = jsonAll.indexOf('\n', i);
			if (j == -1) break;
			int k = jsonAll.indexOf('\"', i);
			String key = jsonAll.substring(i, k);
			int sv = key.indexOf('@');
			int si = key.indexOf('@', sv + 1);
			String cn = key.substring(0, sv);
			String idx = dd.gson.fromJson("\"" + key.substring(si + 1) + "\"", String.class);
			int v = Integer.parseInt(key.substring(sv + 1, si));
			d.createItem(cn, idx, v, jsonAll.substring(k + 2, j - 1) );
			i = j + 4;
		}
		return d;
	}

	private void createItem(String className, String id, int version, String json) throws Exception {
		if (id == null || id.length() == 0) return;
		DocumentDescriptor.ItemDescriptor itd = _documentDescriptor.itemDescriptor(className);
		if (itd == null) return;
		Item item = (Item) itd.itemClass.newInstance();
		item._document = this;
		item._itemDescriptor = itd;
		item._status = 0;
		item._version = version;
		item._json = json;
		item._id = id;
		item.insert();
	}

	private transient DocumentDescriptor _documentDescriptor;
	private transient String _id;
	public transient int _documentVersion;
	private transient int _version;
	private transient String _json;
	private transient String _json2;
	private transient ItemMap[] _items;
	private transient boolean _hasChanged = false;
		
	public int version() { return _documentVersion; }
	public String id() { return _id; }
	public boolean hasChanged(){ return _hasChanged; }
	
	public Item getItem(String className, String id) throws Exception {
		if (id == null || id.length() == 0) return null;
		DocumentDescriptor.ItemDescriptor itd = _documentDescriptor.itemDescriptor(className);
		if (itd == null) return null;
		ItemMap im = _items[itd.index];
		if (im == null){
			im = new ItemMap();
			_items[itd.index] = im;
		}
		Item item = im.get(id);
		if (item == null){
			item = (Item) itd.itemClass.newInstance();
			item._document = this;
			item._itemDescriptor = itd;
			item._status = 2;
			item._id = id;
			item.insert();
			return item;
		}
		Item item2 = null;
		synchronized (_documentDescriptor.gson) {
			try {
				item2 = (Item)_documentDescriptor.gson.fromJson(item._json, itd.itemClass);
			} catch (JsonSyntaxException e){
				throw new Exception("Json syntax - Document/Item:" 
						+ _documentDescriptor.documentClassName + "/" + className, e);
			}
		}
		item2._document = this;
		item2._itemDescriptor = itd;
		item2._status = 1;
		item2._id = id;
		item2._json = item._json;
		_items[itd.index].put(id, item2);
		return item2;
	}
		
	public String serialize(int vmin){
		StringBuffer sb = new StringBuffer();
		synchronized (_documentDescriptor.gson) {
			_json2 = _documentDescriptor.gson.toJson(this);
		}
		_hasChanged = !_json2.equals(_json);
		if (_hasChanged)
			_version = _documentVersion + 1;
		if (vmin >= _version) {
			sb.append("null\n");
		} else {
			sb.append(_json2);
			sb.setLength(sb.length() - 1);
			sb.append(",\"_v\"=").append(_version).append("}\n");
		}
		for(int i = 0; i < _documentDescriptor.idList.size(); i++){
			DocumentDescriptor.ItemDescriptor itd = _documentDescriptor.idList.get(i);
			ItemMap im = _items[i];
			for(String id : im.keySet()){
				Item item = im.get(id);
				if (item._status == 2) continue; // créé pas sauvé
				boolean mod = item.modified();
				if (mod) {
					item._version = _documentVersion + 1;
					_hasChanged = true;
				}
				if (vmin >= item._version) continue;
				String name = _documentDescriptor.gson.toJson(itd.itemClassName + "@" + item._version + "@" + item._id);
				sb.append(",{").append(name).append("=").append(mod ? item._json2 : item._json).append("}\n");
			}
		}
		sb.append("]");
		int vv = _hasChanged ? _documentVersion + 1 : _documentVersion;
		sb.insert(0, "[" + vv + "\n,");
		return sb.toString();
	}
	
	public abstract static class Item {
		private transient Document _document;
		private transient DocumentDescriptor.ItemDescriptor _itemDescriptor;
		private transient String _json;
		private transient String _json2;
		private transient int _status; // 0:nodata, 1:data, 2:new, 3:tosave
		private transient String _id;
		private transient int _version;
		
		public int version() { return _version; }
		
		private String toJson(){
			synchronized (_document._documentDescriptor.gson) {
				return _document._documentDescriptor.gson.toJson(this);
			}
		}
		
		private void insert(){
			int i = _itemDescriptor.index;
			ItemMap im = _document._items[i];
			if (im == null) {
				_document._items[i] = new ItemMap();
				im = _document._items[i];
			}
			im.put(_id, this);
		}
		
		public boolean modified() {
			if (_status != 3) return false;
			if (_json2 == null){
				_json2 = toJson();
				if (_json2.equals(_json)) {
					_json2 = null;
					_status = 1;
					return false;
				} else
					return true;
			} else
				return true;
		}
		
		public void save(){
			_status = 3;
		}
		
	}
	
}
