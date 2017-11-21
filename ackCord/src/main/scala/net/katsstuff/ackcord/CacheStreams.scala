/*
 * This file is part of AckCord, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 Katrix
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
package net.katsstuff.ackcord

import scala.collection.mutable
import scala.concurrent.Future

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import net.katsstuff.ackcord.handlers.CacheSnapshotBuilder
import net.katsstuff.ackcord.http.websocket.gateway.GatewayEvent.ReadyData

object CacheStreams {

  def cacheStreams[D](
      implicit system: ActorSystem,
      mat: Materializer
  ): (Sink[CacheUpdate[D], NotUsed], Source[(CacheUpdate[D], CacheState), NotUsed]) = {
    val (sink, source) = MergeHub
      .source[CacheUpdate[D]](perProducerBufferSize = 16)
      .via(cacheUpdater[D])
      .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
      .run()

    (sink, source)
  }

  def createApiMessages[D]: Flow[(CacheUpdate[D], CacheState), APIMessage, NotUsed] = {
    Flow[(CacheUpdate[D], CacheState)]
      .collect {
        case (APIMessageCacheUpdate(_, sendEvent, _), state) => sendEvent(state)
      }
      .flatMapConcat(_.fold[Source[APIMessage, NotUsed]](Source.empty)(Source.single))
  }

  def sendHandledData[D]: Sink[(CacheUpdate[D], CacheState), Future[Done]] = {
    Sink.foreach[(CacheUpdate[D], CacheState)] {
      case (SendHandledDataCacheUpdate(_, _, findData, sendTo), state) => findData(state).foreach(sendTo ! _)
      case _                                                     =>
    }
  }

  def cacheUpdater[D](
      implicit system: ActorSystem
  ): Flow[CacheUpdate[D], (CacheUpdate[D], CacheState), NotUsed] = {
    var state: CacheState = null

    /**
      * We only handle events when we are ready to, and we have received
      * the ready event.
      */
    def isReady: Boolean = state != null

    Flow[CacheUpdate[D]].statefulMapConcat { () => update =>
      val newState = update match {
        case readyEvent @ APIMessageCacheUpdate(_: ReadyData, _, _) =>
          val builder = new CacheSnapshotBuilder(
            null, //The event will populate this,
            mutable.Map.empty,
            mutable.Map.empty,
            mutable.Map.empty,
            mutable.Map.empty,
            mutable.Map.empty,
            mutable.Map.empty,
            mutable.Map.empty,
            mutable.Map.empty,
          )

          readyEvent.handle(builder)(system.log)

          val snapshot = builder.toImmutable
          CacheState(snapshot, snapshot)
        case handlerEvent: CacheUpdate[_] if isReady =>
          val builder = CacheSnapshotBuilder(state.current)
          handlerEvent.handle(builder)(system.log)

          state.update(builder.toImmutable)
        case _ if !isReady =>
          system.log.error("Received event before ready")
          state
      }

      state = newState

      List(update -> newState)
    }
  }
}