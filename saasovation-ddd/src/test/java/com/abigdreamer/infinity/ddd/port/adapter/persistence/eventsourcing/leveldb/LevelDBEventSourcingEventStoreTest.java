//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.abigdreamer.infinity.ddd.port.adapter.persistence.eventsourcing.leveldb;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.abigdreamer.infinity.ddd.domain.model.DomainEvent;
import com.abigdreamer.infinity.ddd.domain.model.DomainEventPublisher;
import com.abigdreamer.infinity.ddd.event.TestableDomainEvent;
import com.abigdreamer.infinity.ddd.event.sourcing.DispatchableDomainEvent;
import com.abigdreamer.infinity.ddd.event.sourcing.EventStore;
import com.abigdreamer.infinity.ddd.event.sourcing.EventStoreAppendException;
import com.abigdreamer.infinity.ddd.event.sourcing.EventStoreException;
import com.abigdreamer.infinity.ddd.event.sourcing.EventStream;
import com.abigdreamer.infinity.ddd.event.sourcing.EventStreamId;
import com.abigdreamer.infinity.ddd.port.adapter.persistence.eventsourcing.leveldb.LevelDBEventStore;

import junit.framework.TestCase;


public class LevelDBEventSourcingEventStoreTest extends TestCase {

    private EventStore eventStore;

    public LevelDBEventSourcingEventStoreTest() {
        super();
    }

    public void testConnectAndClose() throws Exception {
        assertNotNull(eventStore);
    }

    public void testAppend() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 2; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        EventStream eventStream = this.eventStore.fullEventStreamFor(eventId);

        assertEquals(2, eventStream.version());
        assertEquals(2, eventStream.events().size());

        for (int idx = 1; idx <= 2; ++idx) {
            DomainEvent domainEvent = eventStream.events().get(idx - 1);

            assertEquals(idx, ((TestableDomainEvent) domainEvent).id());
        }
    }

    public void testAppendWrongVersion() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 10; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        EventStream eventStream = this.eventStore.fullEventStreamFor(eventId);

        assertEquals(10, eventStream.version());
        assertEquals(10, eventStream.events().size());

        events.clear();
        events.add(new TestableDomainEvent(11, "Name: " + 11));

        for (int idx = 0; idx < 3; ++idx) {
            try {
                this.eventStore.appendWith(eventId.withStreamVersion(8 + idx), events);

                fail("Should have thrown an exception.");

            } catch (EventStoreAppendException e) {
                // good
            }
        }

        // this should succeed

        this.eventStore.appendWith(eventId.withStreamVersion(11), events);
    }

    public void testEventsSince() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 10; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        List<DispatchableDomainEvent> loggedEvents = this.eventStore.eventsSince(2);

        assertEquals(8, loggedEvents.size());
    }

    public void testEventStreamSince() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 10; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        for (int idx = 10; idx >= 1; --idx) {
            EventStream eventStream = this.eventStore.eventStreamSince(eventId.withStreamVersion(idx));

            assertEquals(10, eventStream.version());
            assertEquals(10 - idx + 1, eventStream.events().size());

            DomainEvent domainEvent = eventStream.events().get(0);

            assertEquals(idx, ((TestableDomainEvent) domainEvent).id());
        }

        try {
            this.eventStore.eventStreamSince(eventId.withStreamVersion(11));

            fail("Should have thrown an exception.");

        } catch (EventStoreException e) {
            // good
        }
    }

    public void testFullEventStreamForStreamName() throws Exception {
        assertNotNull(this.eventStore);

        List<DomainEvent> events = new ArrayList<DomainEvent>();

        for (int idx = 1; idx <= 3; ++idx) {
            events.add(new TestableDomainEvent(idx, "Name: " + idx));
        }

        EventStreamId eventId = new EventStreamId(UUID.randomUUID().toString());

        this.eventStore.appendWith(eventId, events);

        EventStream eventStream = this.eventStore.fullEventStreamFor(eventId);

        assertEquals(3, eventStream.version());
        assertEquals(3, eventStream.events().size());

        events.clear();
        events.add(new TestableDomainEvent(4, "Name: " + 4));

        this.eventStore.appendWith(eventId.withStreamVersion(4), events);

        eventStream = this.eventStore.fullEventStreamFor(eventId);

        assertEquals(4, eventStream.version());
        assertEquals(4, eventStream.events().size());

        for (int idx = 1; idx <= 4; ++idx) {
            DomainEvent domainEvent = eventStream.events().get(idx - 1);

            assertEquals(idx, ((TestableDomainEvent) domainEvent).id());
        }
    }

    @Override
    protected void setUp() throws Exception {
        this.eventStore = LevelDBEventStore.instance("/data/leveldb/esEventStore");

        DomainEventPublisher.instance().reset();

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        this.eventStore.purge();

        this.eventStore.close();

        super.tearDown();
    }
}
