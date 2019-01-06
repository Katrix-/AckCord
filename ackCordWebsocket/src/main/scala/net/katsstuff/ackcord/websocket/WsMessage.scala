/*
 * This file is part of AckCord, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.katsstuff.ackcord.websocket

import cats.Eval
import io.circe.{Decoder, Encoder}
import net.katsstuff.ackcord.util.{JsonOption, JsonUndefined}

/**
  * Represents a message sent by websocket handlers
  *
  * @tparam D The data in this message
  * @tparam OpCode The opcode used by this websocket
  */
private[websocket] trait WsMessage[D, OpCode] {

  /**
    * The op code for the message.
    */
  def op: OpCode

  /**
    * The data for the message.
    */
  def d: Eval[Decoder.Result[D]]

  /**
    * A sequence number for the message if there is one.
    */
  def s: JsonOption[Int] = JsonUndefined

  /**
    * An encoder for the message.
    */
  def dataEncoder: Encoder[D]
}