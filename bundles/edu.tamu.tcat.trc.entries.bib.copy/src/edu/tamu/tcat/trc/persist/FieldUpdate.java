package edu.tamu.tcat.trc.persist;


public class FieldUpdate<T>
{
   private final String fieldId;
   private boolean updated = false;
   private T value = null;

   public FieldUpdate(String fieldId)
   {
      this.fieldId = fieldId;
   }

   public String getFieldId()
   {
      return fieldId;
   }

   public boolean isSet()
   {
      return updated;
   }

   public void set(T obj)
   {
      updated = true;
      value = obj;
   }

   public T get()
   {
      return value;
   }

   // TODO support field level change notification
}
