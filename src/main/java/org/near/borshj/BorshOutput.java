/* This is free and unencumbered software released into the public domain. */

package org.near.borshj;

import static java.util.Objects.requireNonNull;

import androidx.annotation.NonNull;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public interface BorshOutput<Self> {
  default public @NonNull Self write(final @NonNull Object object) {
    requireNonNull(object);
    if (object instanceof Byte) {
      return this.writeU8((byte)object);
    }
    else if (object instanceof Short) {
      return this.writeU16((short)object);
    }
    else if (object instanceof Integer) {
      return this.writeU32((int)object);
    }
    else if (object instanceof Long) {
      return this.writeU64((long)object);
    }
    else if (object instanceof Float) {
      return this.writeF32((float)object);
    }
    else if (object instanceof Double) {
      return this.writeF64((double)object);
    }
    else if (object instanceof BigInteger) {
      return this.writeU128((BigInteger)object);
    }
    else if (object instanceof String) {
      return this.writeString((String)object);
    }
    else if (object instanceof Optional) {
      return (Self)this.writeOptional((Optional)object);
    }
    else if (object instanceof Borsh) {
      return this.writePOJO(object);
    }
    throw new IllegalArgumentException();
  }

  default public @NonNull Self writePOJO(final @NonNull Object object) {
    try {
      for (final Field field : object.getClass().getDeclaredFields()) {
        this.write(field.get(object));
      }
    }
    catch (IllegalAccessException error) {
      throw new RuntimeException(error);
    }
    return (Self)this;
  }

  default public @NonNull Self writeU8(final int value) {
    return this.writeU8((byte)value);
  }

  default public @NonNull Self writeU8(final byte value) {
    return this.write(value);
  }

  default public @NonNull Self writeU16(final int value) {
    return this.writeU16((short)value);
  }

  public @NonNull Self writeU16(final short value);

  public @NonNull Self writeU32(final int value);

  public @NonNull Self writeU64(final long value);

  default public @NonNull Self writeU128(final long value) {
    return this.writeU128(BigInteger.valueOf(value));
  }

  default public @NonNull Self writeU128(final @NonNull BigInteger value) {
    if (value.signum() == -1) {
      throw new ArithmeticException("integer underflow");
    }
    if (value.bitLength() > 128) {
      throw new ArithmeticException("integer overflow");
    }
    final byte[] bytes = value.toByteArray();
    for (int i = bytes.length - 1; i >= 0; i--) {
      this.write(bytes[i]);
    }
    for (int i = 0; i < 16 - bytes.length; i++) {
      this.write((byte)0);
    }
    return (Self)this;
  }

  public @NonNull Self writeF32(final float value);

  public @NonNull Self writeF64(final double value);

  default public @NonNull Self writeString(final @NonNull String string) {
    final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
    this.writeU32(bytes.length);
    return this.write(bytes);
  }

  default public @NonNull Self writeFixedArray(final @NonNull byte[] array) {
    return this.write(array);
  }

  default public @NonNull Self writeArray(final @NonNull Object[] array) {
    // TODO
    return (Self)this;
  }

  default public @NonNull <T> Self writeOptional(final @NonNull Optional<T> optional) {
    if (optional.isPresent()) {
      this.writeU8(1);
      return this.write(optional.get());
    }
    else {
      return this.writeU8(0);
    }
  }

  public @NonNull Self write(final @NonNull byte[] bytes);

  public @NonNull Self write(final byte b);
}
