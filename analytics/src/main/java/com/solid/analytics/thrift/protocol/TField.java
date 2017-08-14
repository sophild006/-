/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solid.analytics.thrift.protocol;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Helper class that encapsulates field metadata.
 *
 */
public class TField {
  public TField() {}

  public TField(String n, byte t, short i) {
    name = n;
    type = t;
    id = i;
  }

  public String name = "";
  public byte   type = TType.STOP;
  public short  id   = 0;

  private static Decrypter decrypter;

  public static void initDecrypter(byte[] key, byte[] iv) throws Exception {
    decrypter = new Decrypter(key, iv);
  }

  private String n = null;

  public String name() {
    if (decrypter == null)
      return name;

    if (n != null)
      return n;

    try {
      n = decrypter.decrypt(name);
      return n;
    } catch (Exception e) {
      e.printStackTrace();
      return name;
    }
  }

  static class Decrypter {

    final Cipher cipher;

    Decrypter(byte[] key, byte[] iv) throws Exception {
      SecretKeySpec k = new SecretKeySpec(key, "AES");
      AlgorithmParameterSpec i = new IvParameterSpec(iv);
      cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, k, i);
    }

    String decrypt(String n) throws Exception {
      byte[] bs = fromHex(n);
      byte[] es = cipher.doFinal(bs);
      return new String(es, "utf-8");
    }

    public static int fromHex(char c) {
      if (c >= 'a' && c <= 'f')
        return (c - 'a') + 10;
      if (c >= 'A' && c <= 'F')
        return (c - 'A') + 10;
      if (c >= '0' && c <= '9')
        return (c - '0');

      throw new IllegalArgumentException("must be hex!");
    }

    public static byte[] fromHex(String hex) {
      if (hex == null)
        return null;

      if (hex.length() % 2 != 0)
        throw new IllegalArgumentException("length must be even!");

      byte[] bin = new byte[hex.length() / 2];
      for (int i = 0; i < bin.length; i++) {
        bin[i] = (byte) ((fromHex(hex.charAt(i * 2)) << 4) | (fromHex(hex.charAt(i * 2 + 1))));
      }

      return bin;
    }
  }
}
