/* Poly2Tri
 * Copyright (c) 2009, Mason Green
 * http://code.google.com/p/poly2tri/
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Poly2Tri nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.poly2tri

import scala.collection.mutable.{ArrayBuffer, Queue}

// Doubly linked list
class MonotoneMountain {

	var tail, head: Point = null
	var size = 0

	val convexPoints = new Queue[Point]
    // Monotone mountain points
	val monoPoly = new ArrayBuffer[Point]
    // Triangles that constitute the mountain
	val triangles = new ArrayBuffer[Array[Point]]
	      
	var angle = 0f
 
	// Append a point to the list
	def +=(point: Point) {
	  size match {
	    case 0 => 
	      head = point
        case 1 =>
          tail = point
		  tail.prev = head
		  head.next = tail
        case _ =>
          tail.next = point
          point.prev = tail
          tail = point
	  }
	  size += 1
	}

	// Remove a point from the list
	def remove(point: Point) {
		val next = point.next
		val prev = point.prev
		point.prev.next = next
		point.next.prev = prev
		size -= 1
	}
 
	// Partition a x-monotone mountain into triangles O(n)
	// See "Computational Geometry in C", 2nd edition, by Joseph O'Rourke, page 52
	def triangulate {
	
	  // create monotone polygon - for dubug purposes
	  genMonoPoly
   
	  if(size == 3) {
	    lastTriangle
	  } else {
	   // Initialize internal angles at each nonbase vertex
	   var p = head.next
	   while(p != tail) {
	     // Link strictly convex vertices into a list
	     if(convex(p)) convexPoints.enqueue(p)
	     p = p.next
	   }
    
	   while(!convexPoints.isEmpty) {
	     
	     val ear = convexPoints.dequeue
	     val a = ear.prev.clone
	     val b = ear
	     val c = ear.next.clone
	     val triangle = Array(a, b, c)
	     triangles += triangle
	     
	     // Remove ear, update angles and convex list
	     remove(ear) 
	     if(a.prev != null && convex(a)) convexPoints.enqueue(a); 
         if(c.prev != null && convex(c)) convexPoints.enqueue(c)

	   }    
	   assert(size <= 3, "Triangulation bug")
	   if(size == 3)lastTriangle
	}
   }
 
	// Return the monotone polygon 
	private def genMonoPoly { 
      var p = head
	  while(p != null) {
	      monoPoly += p
	      p = p.next
	  }
	}
 
	// Determines if the inslide angle between edge v2-v3 and edge v2-v1 is convex 
	private def convex(p: Point) = {
	  val a = (p.next - p) 
	  val b = (p.prev - p) 
      angle = Math.atan2(b.y,b.x).toFloat - Math.atan2(a.y,a.x).toFloat
      if(angle < 0) while(angle < -Math.Pi) angle += Math.Pi.toFloat
      if(angle > 0) while(angle > Math.Pi) angle -= Math.Pi.toFloat
      // For numerical robustness....
      angle = 0.01f * Math.round( angle * 10.0f)
      if(p.y >= head.y) {
        (angle < 0)
      } else {
        !(angle < 0)
      }
    }

	private def lastTriangle {
	  val triangle = new Array[Point](3)
	  var i = 0
      var p = head
	  while(p != null) {
	      triangle(i) = p
	      p = p.next
          i += 1
	  }
	  triangles += triangle
	}
}