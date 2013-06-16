/*
Android Rivers is an app to read and discover news using RiverJs, RSS and OPML format.
Copyright (C) 2012 Dody Gunawinata (dodyg@silverkeytech.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package com.silverkeytech.news_engine.syndications.atom

import java.util.ArrayList


public class AtomBuilder (){
    val feed = Feed()
    public var author : PersonElementBuilder = PersonElementBuilder(feed.author!!)
    public val entry: EntryBuilder = EntryBuilder(feed)

    public fun build() : Feed {
        return feed
    }

    public fun setId(id : String){
        feed.id = id
    }

    public fun setTitle(title : String){
        feed.title = title
    }

    public fun setUpdated(updated : String){
        feed.updated = updated
    }

    public fun setIcon(icon : String){
        feed.icon = icon
    }

    public fun setLogo(logo : String){
        feed.logo = logo
    }

    public fun setSubtitle(subTitle : String){
        feed.subtitle = subTitle
    }


    public class PersonElementBuilder(val author : ArrayList<PersonElement>){
        private var person : PersonElement = PersonElement()

        public fun startItem(){
            person = PersonElement()
        }

        public fun endItem(){
            author.add(person)
        }

        public fun setName(name : String){
            person.name = name
        }

        public fun setUri(uri : String){
            person.uri = uri
        }

        public fun setEmail(email : String){
            person.email = email
        }
    }

    public class EntryBuilder(private val feed: Feed){
        var entry : Entry = Entry()
        public var author : PersonElementBuilder = PersonElementBuilder(entry.author!!)

        public fun setId(id : String){
            entry.id = id
        }

        public fun setTitle(title : String){
            entry.title = title
        }

        public fun setUpdated(updated : String){
            entry.updated = updated
        }

        public fun setPublished(published : String){
            entry.published = published
        }

        public fun startItem(){
            entry = Entry()
            author = PersonElementBuilder(entry.author!!)
        }

        public fun endItem(){
            feed.entry!!.add(entry)
        }
    }
}