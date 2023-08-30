/*
 * Copyright 2011 Google Inc.
 * Copyright 2012 Matt Corallo.
 * Copyright 2014 Andreas Schildbach
 * Copyright 2017 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bearlycattable.bait.commons.extern.bitcoinjExtern;

/** Enumeration to encapsulate the type of this script. */
public enum ScriptType {
    P2PKH(1), // pay to pubkey hash (aka pay to address)
    P2PK(2), // pay to pubkey
    P2SH(3), // pay to script hash
    P2WPKH(4), // pay to witness pubkey hash
    P2WSH(5), // pay to witness script hash
    P2TR(6); // pay to taproot

    public final int id;

    private ScriptType(int id) {
        this.id = id;
    }
}
