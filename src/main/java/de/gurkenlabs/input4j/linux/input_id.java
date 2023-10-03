package de.gurkenlabs.input4j.linux;

import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

import static java.lang.foreign.ValueLayout.JAVA_SHORT;

/*
 * IOCTLs (0x00 - 0x7f)
 */
class input_id {
  public short bustype;
  public short vendor;
  public short product;
  public short version;

  static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
          JAVA_SHORT.withName("bustype"),
          JAVA_SHORT.withName("vendor"),
          JAVA_SHORT.withName("product"),
          JAVA_SHORT.withName("version")
  ).withName("input_id");

  static final VarHandle VH_bustype = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("bustype"));
  static final VarHandle VH_vendor = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("vendor"));
  static final VarHandle VH_product = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("product"));
  static final VarHandle VH_version = $LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("version"));

  public static input_id read(MemorySegment segment) {
    var inputId = new input_id();
    inputId.bustype = (short) VH_bustype.get(segment);
    inputId.vendor = (short) VH_vendor.get(segment);
    inputId.product = (short) VH_product.get(segment);
    inputId.version = (short) VH_version.get(segment);

    return inputId;
  }

  public void write(MemorySegment segment) {
    VH_bustype.set(segment, bustype);
    VH_vendor.set(segment, vendor);
    VH_product.set(segment, product);
    VH_version.set(segment, version);
  }
}
