package serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import serial.DocumentMeta.DMException;
import serial.DocumentMeta.FieldDescriptor;

public abstract class Document {
	transient DocumentMeta _meta;
	
	public FieldDescriptor fieldDescriptor(String fieldName){
		return _meta.fieldDescriptor(fieldName);
	}

	public long getAsLong(String fieldName){
		return _meta.fieldDescriptor(fieldName).getAsLong(this);
	}

	public int getAsInt(String fieldName){
		return _meta.fieldDescriptor(fieldName).getAsInt(this);
	}

	public double getAsDouble(String fieldName){
		return _meta.fieldDescriptor(fieldName).getAsDouble(this);
	}

	public String getAsString(String fieldName){
		return _meta.fieldDescriptor(fieldName).getAsString(this);
	}

	public long[] getAsAL(String fieldName){
		return _meta.fieldDescriptor(fieldName).getAsAL(this);
	}

	public int[] getAsAI(String fieldName){
		return _meta.fieldDescriptor(fieldName).getAsAI(this);
	}

	public double[] getAsAD(String fieldName){
		return _meta.fieldDescriptor(fieldName).getAsAD(this);
	}

	public String[] getAsAS(String fieldName){
		return _meta.fieldDescriptor(fieldName).getAsAS(this);
	}

	public Collection<Long> getAsCL(String fieldName) {
		return _meta.fieldDescriptor(fieldName).getAsCL(this);
	}
	
	public Collection<Integer> getAsCI(String fieldName) {
		return _meta.fieldDescriptor(fieldName).getAsCI(this);
	}

	public Collection<Double> getAsCD(String fieldName) {
		return _meta.fieldDescriptor(fieldName).getAsCD(this);
	}

	public Collection<String> getAsCS(String fieldName) {
		return _meta.fieldDescriptor(fieldName).getAsCS(this);
	}

	public void set(String fieldName, long value){
		_meta.fieldDescriptor(fieldName).set(this, value);
	}

	public void set(String fieldName, int value){
		_meta.fieldDescriptor(fieldName).set(this, value);
	}

	public void set(String fieldName, double value){
		_meta.fieldDescriptor(fieldName).set(this, value);
	}

	public void set(String fieldName, String value){
		_meta.fieldDescriptor(fieldName).set(this, value);
	}
	
	public void set(String fieldName, long[] value) {
		_meta.fieldDescriptor(fieldName).set(this, value);
	}

	public void set(String fieldName, int[] value) {
		_meta.fieldDescriptor(fieldName).set(this, value);
	}

	public void set(String fieldName, double[] value) {
		_meta.fieldDescriptor(fieldName).set(this, value);
	}

	public void set(String fieldName, String[] value) {
		_meta.fieldDescriptor(fieldName).set(this, value);
	}

	public void setCL(String fieldName, Collection<Long> value) {
		_meta.fieldDescriptor(fieldName).setCL(this, value);
	}
	
	public void setCI(String fieldName, Collection<Integer> value) {
		_meta.fieldDescriptor(fieldName).setCI(this, value);
	}
	
	public void setCD(String fieldName, Collection<Double> value) {
		_meta.fieldDescriptor(fieldName).setCD(this, value);
	}

	public void setCS(String fieldName, Collection<String> value) {
		_meta.fieldDescriptor(fieldName).setCS(this, value);
	}

	/********************************************************************/
	
	public static Class<?> forName(String name) throws DMException {
		return DocumentMeta.forName(name);
	}
	
	public static void register(Class<?>... documentClasses) throws DMException{
		DocumentMeta.register(documentClasses);
	}
	public static Document newDocument(Class<?> clazz, String id) throws DMException {
		return DocumentMeta.newDocument(clazz, id);
	}
	public static Document newDocument(String jsonAll, boolean full) throws Exception {
		return DocumentMeta.newDocument(jsonAll, full);
	}

	public Document.Item getItem(Class<?> clazz, String id) throws Exception {
		return _meta.getItem(clazz, id);
	}
	public Set<String> getIds(Class<?> clazz) {
		return _meta.getIds(clazz);
	}

	/*
	 * trStamp : stamp de la transaction et nouvelle version du document s'il a changé
	 * full : force une sérialisation complète
	 */
	String serializeForDB(long trStamp, boolean full){
		return _meta.serializeForDB(trStamp, full);
	}
	
	/*
	 * _stamp : stamp de la dernière vérification de non évolution (ou d'évolution)
	 * vmin : stamp de la dernière synchronisation
	 */
	String serializeForSync(long vmin){
		return _meta.serializeForSync(vmin);
	}

	public long version() { return _meta.version(); }
	public long stamp() { return _meta.stamp(); }
	public String id() { return _meta.id(); }

	public static abstract class Item {
		transient DocumentMeta.ItemMeta _meta;
		public void save(){	_meta.save(); }
		public void deleted(){	_meta.delete(); }
		public DocumentMeta.Status modified() { return _meta.status(); }
		public long version() { return _meta.version(); }
		public String id() { return _meta.id(); }
	}
	
	public static byte[] Gzip(byte[] bytes) {
		try {
			int max = 1;
			byte[] bytes2 = null;
			long t5 = System.currentTimeMillis();
			for(int i = 0; i < max; i++) {
				ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
				GZIPOutputStream zos = new GZIPOutputStream(bos2, 128 * 1024);
				zos.write(bytes);
				zos.close();
				bytes2 = bos2.toByteArray();
			}
			long t6 = System.currentTimeMillis();
			System.out.println("GZIP : " + (t6 - t5) + "ms. " + bytes.length + " / " + bytes2.length);
			return bytes2;
		} catch(Exception e){
			return new byte[0];
		}
	}

	public static byte[] Gunzip(byte[] bytes) {
		try {
			int max = 1;
			byte[] bytes2 = null;
			byte[] buf = new byte[4096];
			long t5 = System.currentTimeMillis();
			for(int i = 0; i < max; i++) {
				ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
				ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				GZIPInputStream gzis = new GZIPInputStream(bis, 128 * 1024);
				int len;
		        while ((len = gzis.read(buf)) > 0) {
		        	bos2.write(buf, 0, len);
		        }
				gzis.close();
				bytes2 = bos2.toByteArray();
			}
			long t6 = System.currentTimeMillis();
			System.out.println("GUNZIP : " + (t6 - t5) + "ms. " + bytes.length + " / " + bytes2.length);
			return bytes2;
		} catch(Exception e){
			return new byte[0];
		}
	}

}
