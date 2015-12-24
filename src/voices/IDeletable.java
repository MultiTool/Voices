/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voices;

/**
 *
 * @author MultiTool
 * 
 * IDeletable exists to help porting to a non-garbage-collecting environment such as C++, It helps do accounting for memory leaks. 
 * We will probably switch this out for a reference counting scheme. Ref counting will only be useful for objects with multiple references such as Songlets. 
 * 
 */
public interface IDeletable {
  boolean Create_Me();
  void Delete_Me();
//  int Ref_Me();// increment ref counter and return new value just for kicks
//  int UnRef_Me();// decrement ref counter and return new value just for kicks
}
//  Possible pattern:
//  (MyPointer = DeletableObject).Ref_Me();// ref pattern 
//  MyPointer = MyPointer.UnRef_Me();// unref pattern, unref returns null?

