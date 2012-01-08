/*
 * This file is part of SpoutAPI (http://www.getspout.org/).
 *
 * SpoutAPI is licensed under the SpoutDev license version 1.
 *
 * SpoutAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * SpoutAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev license version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://getspout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.getspout.api.model;

public class Model {
	BoneTransform modelRoot = new BoneTransform();
	Bone root = new Bone("root", modelRoot);
   
	/**
	 * Attaches a Mesh to this model at the provided Bone name
	 * @param attachTo
	 * @param boneName
	 * @param mesh
	 */
	void attachMesh(String attachTo, String boneName, Mesh mesh){
		Bone bone = root.getBone(attachTo);
		if(bone == null) throw new BoneNotFoundException("Bone " + attachTo + " Not found");
		bone.attachBone(boneName, mesh);
	
	}
	/**
	 * Attaches a mesh to the Root bone.  
	 * @param boneName
	 * @param mesh
	 */
	void attachMesh(String boneName, Mesh mesh){
		this.attachMesh("root", boneName, mesh);
	}
	
	
}
