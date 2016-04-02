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
		dd.documentClass = documentClass;
		dd.innerClasses = dd.documentClass.getDeclaredClasses();
		dd.innerClassNames = new String[dd.innerClasses.length];
		for(int i = 0; i < dd.innerClasses.length; i++)
			dd.innerClassNames[i] = dd.innerClasses[i].getSimpleName();
		dd.gson = new Gson();
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
			DocumentDescriptor dd = new DocumentDescriptor();
			dd.documentClassName = className;
			ddList.add(dd);
			return dd;
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
		private Gson gson;
		
	}
	
	private transient DocumentDescriptor documentDescriptor;
	public transient String id;
	public transient int version;
	private transient ItemMap[] items;
	
	public Document(String id, int version){
		documentDescriptor = DocumentDescriptor.documentDescriptor(this.getClass().getSimpleName());
		this.id = id;
		this.version = version;
		items = new ItemMap[documentDescriptor.idList.size()];
	}
	public int version() { return version; }
	public String id() { return id; }
	
	
	public Item fromJson(String json, String className) throws Exception {
		DocumentDescriptor.ItemDescriptor id = documentDescriptor.itemDescriptor(className);
		if (id == null) return null;
		synchronized (documentDescriptor.gson) {
			try {
				Item item = (Item)documentDescriptor.gson.fromJson(json, id.itemClass);
				item.document = this;
				item.itemDescriptor = id;
				item.json = json;
				item.id = item.id();
				item.insert();
			return item;
			} catch (JsonSyntaxException e){
				throw new Exception("Json syntax - Document/Item:" 
						+ documentDescriptor.documentClassName + "/" + className, e);
			}
		}
	}

	public Item newItem(String className) throws Exception {
		DocumentDescriptor.ItemDescriptor id = documentDescriptor.itemDescriptor(className);
		if (id == null) return null;
		Item item = (Item) id.itemClass.newInstance();
		item.document = this;
		item.itemDescriptor = id;
		return item;
	}

	public String fullSerialize(){
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		synchronized (documentDescriptor.gson) {
			sb.append(documentDescriptor.gson.toJson(this));
		}
		sb.setLength(sb.length() -1);
		sb.append(",\"v\"=").append(version).append("}\n");		
		for(int i = 0; i < documentDescriptor.idList.size(); i++){
			DocumentDescriptor.ItemDescriptor itd = documentDescriptor.idList.get(i);
			ItemMap im = items[i];
			for(String id : im.keySet()){
				Item item = im.get(id);
				int v = item.json2 == null ? version : item.version;
				String name = itd.itemClassName + "@" + v + "@" + item.id; 
				sb.append(",{");
				sb.append(documentDescriptor.gson.toJson(name));
				sb.append("=").append(item.json2).append("}\n");
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
	public abstract static class Item {
		private transient Document document;
		private transient DocumentDescriptor.ItemDescriptor itemDescriptor;
		private transient String json;
		private transient String id;
		private transient int version;
		private transient String json2;
		
		public int version() { return version; }
		public abstract String id();
		
		private String toJson(){
			synchronized (document.documentDescriptor.gson) {
				return document.documentDescriptor.gson.toJson(this);
			}
		}
		
		private void insert(){
			int i = itemDescriptor.index;
			ItemMap im = document.items[i];
			if (im == null) {
				document.items[i] = new ItemMap();
				im = document.items[i];
			}
			im.put(id, this);
		}
		
		public void save(){
			if (id == null) {
				id = id();
				insert();
			}
			json2 = toJson();
			if (json2.equals(json))
				json2 = null;
		}
		
	}
	
}
