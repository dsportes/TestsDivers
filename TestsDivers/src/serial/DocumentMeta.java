package serial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DocumentMeta {
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
		Document doc;
		try {
			doc = (Document)dd.documentClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			String m = "Le document de classe " + cn + " n'est pas instantiable. " + e.getMessage();
			log.severe(m);
			throw new IllegalArgumentException(m);
		}
		synchronized (dd.gson) {
			dd.json = dd.gson.toJson(doc);
		}
	}
	
	@SuppressWarnings("serial")
	private static class ItemMap extends HashMap<String,ItemMeta> {}
	
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
		private String json;
		
	}
	static Document newDocument(String className, String id) throws Exception {
		return newDocument(className, id, null);
	}
	static Document newDocument(String className, String id, String jsonAll) throws Exception {
		DocumentDescriptor dd = DocumentDescriptor.documentDescriptor(className);
		if (dd == null) return null;
		DocumentMeta meta = new DocumentMeta();
		if (jsonAll == null || jsonAll.length() == 0) {
			try { meta.document = (Document)dd.documentClass.newInstance(); } catch (Exception e) {}
			meta.document._meta = meta;
			meta._documentDescriptor = dd;
			meta._id = id;
			meta._documentVersion = 0;
			meta._items = new ItemMap[dd.idList.size()];
			return meta.document;
		}
		int s = jsonAll.indexOf('\n');
		int vv = Integer.parseInt(jsonAll.substring(1, s));
		s = s + 2;
		int i = jsonAll.indexOf('\n', s);
		if ("null".equals(jsonAll.substring(s, i))) {
			try { meta.document = (Document)dd.documentClass.newInstance(); } catch (Exception e) {}
			meta.document._meta = meta;
			meta._documentVersion = vv;
			meta._json = dd.json;
			meta._documentDescriptor = dd;
			meta._id = id;
			meta._items = new ItemMap[dd.idList.size()];
			meta._version = -1;
		} else {
			int vx = jsonAll.lastIndexOf(',', i);
			String x = jsonAll.substring(s, vx) + "}";
			synchronized (dd.gson) {
				meta.document = (Document)dd.gson.fromJson(x, dd.documentClass);
			}
			meta.document._meta = meta;
			meta._documentVersion = vv;
			meta._version = Integer.parseInt(jsonAll.substring(vx + 6, i - 1));
			meta._json = x;
		}
		meta._id = id;
		meta._documentDescriptor = dd;
		meta._items = new ItemMap[dd.idList.size()];
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
			meta.createItemMeta(cn, idx, v, jsonAll.substring(k + 2, j - 1) );
			i = j + 4;
		}
		return meta.document;
	}

	private void createItemMeta(String className, String id, int version, String json) throws Exception {
		if (id == null || id.length() == 0) return;
		DocumentDescriptor.ItemDescriptor itd = _documentDescriptor.itemDescriptor(className);
		if (itd == null) return;
		ItemMeta itemMeta = new ItemMeta();
		itemMeta._document = this;
		itemMeta._itemDescriptor = itd;
		itemMeta._status = 0;
		itemMeta._version = version;
		itemMeta._json = json;
		itemMeta._id = id;
		int i = itemMeta._itemDescriptor.index;
		ItemMap im = itemMeta._document._items[i];
		if (im == null) {
			itemMeta._document._items[i] = new ItemMap();
			im = itemMeta._document._items[i];
		}
		im.put(itemMeta._id, itemMeta);
	}

	private transient DocumentDescriptor _documentDescriptor;
	private transient String _id;
	private transient int _documentVersion;
	private transient int _version;
	private transient String _json;
	private transient String _json2;
	private transient ItemMap[] _items;
	private transient boolean _hasChanged = false;
	private Document document;
		
	int version() { return _documentVersion; }
	String id() { return _id; }
	boolean hasChanged(){ return _hasChanged; }
	
	Set<String> getIds(String className) {
		DocumentDescriptor.ItemDescriptor itd = _documentDescriptor.itemDescriptor(className);
		if (itd != null) {
			ItemMap im = _items[itd.index];
			if (im != null) 
				return im.keySet();
		}
		return new HashSet<String>();
	}
	
	Document.Item getItem(String className, String id) throws Exception {
		if (id == null || id.length() == 0) return null;
		DocumentDescriptor.ItemDescriptor itd = _documentDescriptor.itemDescriptor(className);
		if (itd == null) return null;
		ItemMap im = _items[itd.index];
		if (im == null){
			im = new ItemMap();
			_items[itd.index] = im;
		}
		ItemMeta itemMeta = im.get(id);
		Document.Item item = null;
		if (itemMeta == null){
			itemMeta = new ItemMeta();
			itemMeta._document = this;
			itemMeta._itemDescriptor = itd;
			itemMeta._id = id;
			im.put(id, itemMeta);
		}
		if (itemMeta._json == null) {
			item = (Document.Item) itd.itemClass.newInstance();
			itemMeta._status = 2;
		} else {
			synchronized (_documentDescriptor.gson) {
				try {
					item = (Document.Item)_documentDescriptor.gson.fromJson(itemMeta._json, itd.itemClass);
				} catch (JsonSyntaxException e){
					throw new Exception("Syntaxe JSON - Document/Item:" 
							+ _documentDescriptor.documentClassName + "/" + className, e);
				}
			}
			itemMeta._status = 1;
		}
		item._meta = itemMeta;
		itemMeta.item = item;
		return item;
	}
		
	String serialize(int vmin){
		StringBuffer sb = new StringBuffer();
		synchronized (_documentDescriptor.gson) {
			_json2 = _documentDescriptor.gson.toJson(document);
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
			if (im != null && im.size() != 0) {
				for(String id : im.keySet()){
					ItemMeta itemMeta = im.get(id);
					if (itemMeta._status == 2) continue; // créé pas sauvé
					boolean mod = itemMeta.modified();
					if (mod) {
						itemMeta._version = _documentVersion + 1;
						_hasChanged = true;
					}
					if (vmin >= itemMeta._version) continue;
					String name = _documentDescriptor.gson.toJson(itd.itemClassName + "@" + itemMeta._version + "@" + itemMeta._id);
					sb.append(",{").append(name).append("=").append(mod ? itemMeta._json2 : itemMeta._json).append("}\n");
				}
			}
		}
		sb.append("]");
		int vv = _hasChanged ? _documentVersion + 1 : _documentVersion;
		sb.insert(0, "[" + vv + "\n,");
		return sb.toString();
	}
	
	void commit(){
		if (_hasChanged)
			_documentVersion++;
		if (_version == _documentVersion)
			_json = _json2;
		_json2 = null;
		document = null;
		for(int i = 0; i < _documentDescriptor.idList.size(); i++){
			ItemMap im = _items[i];
			if (im == null || im.size() == 0){
				_items[i] = null;
			} else {
				for(String id : im.keySet()){
					ItemMeta itemMeta = im.get(id);
					if (itemMeta._version == _documentVersion)
						itemMeta._json = itemMeta._json2;
					itemMeta._json2 = null;
					itemMeta.item = null;;
				}
			}
		}
	}
	
	DocumentMeta duplicate(){
		DocumentMeta c = new DocumentMeta();
		c._documentDescriptor = _documentDescriptor;
		c._id = _id;
		c._documentVersion = _documentVersion;
		c._version = _version;
		c._json = _json;
		c._items = new ItemMap[_documentDescriptor.idList.size()];
		for(int i = 0; i < _documentDescriptor.idList.size(); i++){
			ItemMap im = _items[i];
			if (im != null && im.size() == 0){
				ItemMap cim = new ItemMap();
				c._items[i] = cim;
				for(String id : im.keySet()){
					ItemMeta m = im.get(id);
					ItemMeta x = new ItemMeta();
					cim.put(m._id, x);
					x._document = m._document;
					x._itemDescriptor = m._itemDescriptor;
					x._json = m._json;
					x._id = m._id;
					x._version = m._version;
				}
			}
		}
		return c;
	}
	
	public static class ItemMeta {
		private transient DocumentMeta _document;
		private transient DocumentDescriptor.ItemDescriptor _itemDescriptor;
		private transient String _json;
		private transient String _json2;
		private transient int _status; // 0:nodata, 1:data, 2:new, 3:tosave
		private transient String _id;
		private transient int _version;
		private Document.Item item;
		
		int version() { return _version; }
		String id() { return _id; }
		
		boolean modified() {
			if (_status != 3) return false;
			if (_json2 == null){
				synchronized (_document._documentDescriptor.gson) {
					_json2 = _document._documentDescriptor.gson.toJson(item);
				}
				if (_json2.equals(_json)) {
					_json2 = null;
					_status = 1;
					return false;
				} else
					return true;
			} else
				return true;
		}
		
		void save(){
			_status = 3;
		}
		
	}
	
}
