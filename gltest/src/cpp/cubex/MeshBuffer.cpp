/*
 * MeshBuffer.cpp
 *
 *  Created on: 18.06.2013
 *      Author: il
 */

#include <string>

#include <GL3/gl3w.h>

#include "MeshBuffer.h"

using namespace std;

namespace cubex {

MeshBuffer::MeshBuffer(const Mesh &mesh)
{
	// Creating the buffer data for faces

	printf("Extracting data...\n");fflush(stdout);
	verticesCount = 3 * mesh.getFaces().size();

	buffer = new float[8 * 3 * mesh.getFaces().size()];
	int index = 0;

	const glm::vec3* vertices = &(mesh.getVertices()[0]);
	const glm::vec3* normals = &(mesh.getNormals()[0]);
	const glm::vec2* textureCoords = &(mesh.getTextureCoords()[0]);

	for (int k = 0; k < mesh.getFaces().size(); k++)
	{
		Face face = mesh.getFaces()[k];

		glm::vec3 vertex1 = vertices[face.vertexIndex1];
		glm::vec3 vertex2 = vertices[face.vertexIndex2];
		glm::vec3 vertex3 = vertices[face.vertexIndex3];

		glm::vec3 normal1 = glm::vec3(0.0f);
		glm::vec3 normal2 = glm::vec3(0.0f);
		glm::vec3 normal3 = glm::vec3(0.0f);

		if (face.containsNormals)
		{
			normal1 = normals[face.normalIndex1];
			normal2 = normals[face.normalIndex2];
			normal3 = normals[face.normalIndex3];
		}
		else
		{
			// Calculating normals. NOT SMOOTH.

			normal1 = glm::normalize(glm::cross(vertex2 - vertex1, vertex3 - vertex2));
			normal2 = normal1;
			normal3 = normal1;
		}

		glm::vec2 textureCoord1 = glm::vec2(0.0f);
		glm::vec2 textureCoord2 = glm::vec2(0.0f);
		glm::vec2 textureCoord3 = glm::vec2(0.0f);

		if (face.containsTextureCoords)
		{
			textureCoord1 = textureCoords[face.textureCoordIndex1];
			textureCoord2 = textureCoords[face.textureCoordIndex2];
			textureCoord3 = textureCoords[face.textureCoordIndex3];
		}

		// First vertex

		buffer[index++] = vertex1.x;
		buffer[index++] = vertex1.y;
		buffer[index++] = vertex1.z;

		buffer[index++] = normal1.x;
		buffer[index++] = normal1.y;
		buffer[index++] = normal1.z;

		buffer[index++] = textureCoord1.x;
		buffer[index++] = textureCoord1.y;

		// Second vertex

		buffer[index++] = vertex2.x;
		buffer[index++] = vertex2.y;
		buffer[index++] = vertex2.z;

		buffer[index++] = normal2.x;
		buffer[index++] = normal2.y;
		buffer[index++] = normal2.z;

		buffer[index++] = textureCoord2.x;
		buffer[index++] = textureCoord2.y;

		// Third vertex

		buffer[index++] = vertex3.x;
		buffer[index++] = vertex3.y;
		buffer[index++] = vertex3.z;

		buffer[index++] = normal3.x;
		buffer[index++] = normal3.y;
		buffer[index++] = normal3.z;

		buffer[index++] = textureCoord3.x;
		buffer[index++] = textureCoord3.y;
	}

	printf("Loading data into video memory...\n");fflush(stdout);

	// Creating a VAO (Vertex Array Object)
	glGenVertexArrays(1, &VertexArrayID);

	// Binding the VAO
	glBindVertexArray(VertexArrayID);

	// Generate 1 buffer, put the resulting identifier in vertexbuffer
	glGenBuffers(1, &vertexBufferObject);

	// The following commands will talk about our 'vertexbuffer' buffer
	glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);

	// Give our vertices to OpenGL.
	glBufferData(GL_ARRAY_BUFFER, (long)sizeof(GLfloat) * 8 * verticesCount, buffer, GL_STATIC_DRAW);

	printf("Loaded\n");fflush(stdout);

}

void MeshBuffer::connectToShaderProgram(const ShaderProgram* shaderProgram,
                            const string& vertexVec3ShaderVariableName,
                            const string& normalVec3ShaderVariableName,
                            const string& textureVec2ShaderVariableName)
{
	this->shaderProgram = shaderProgram;
	this->vertexVec3ShaderVariableName = vertexVec3ShaderVariableName;
	this->normalVec3ShaderVariableName = normalVec3ShaderVariableName;
	this->textureVec2ShaderVariableName = textureVec2ShaderVariableName;

	if (shaderProgram != NULL)
	{
		if (vertexVec3ShaderVariableName != "")
			vertexVec3ShaderVariableAttrib = shaderProgram->getAttribLocation(vertexVec3ShaderVariableName.c_str());
		if (normalVec3ShaderVariableName != "")
			normalVec3ShaderVariableAttrib = shaderProgram->getAttribLocation(normalVec3ShaderVariableName.c_str());
		if (textureVec2ShaderVariableName != "")
			textureVec2ShaderVariableAttrib = shaderProgram->getAttribLocation(textureVec2ShaderVariableName.c_str());
	}
}

void MeshBuffer::draw()
{
	if (shaderProgram != NULL)
	{
		shaderProgram->use();
	}
	else
	{
		throw CubexException(__FILE__, __LINE__, "Shader program shouldn't be null");
	}

	if (vertexVec3ShaderVariableAttrib)
	glEnableVertexAttribArray(vertexVec3ShaderVariableAttrib);
	glEnableVertexAttribArray(normalVec3ShaderVariableAttrib);
	glEnableVertexAttribArray(textureVec2ShaderVariableAttrib);

	// Setting vertex data
	glVertexAttribPointer(
		vertexVec3ShaderVariableAttrib,					// vertex shader variable
		3,                  							// length of vec3 (XYZ)
		GL_FLOAT,										// type
		GL_FALSE,										// normalized?
		sizeof(GLfloat) * 8,							// stride (the length of each element in mesh buffer array is 8)
		(void*)0										// array buffer offset (we use first 3 of 8)
	);

	// Setting normal data
	glVertexAttribPointer(
		normalVec3ShaderVariableAttrib,					// normal shader variable
		3,												// length of vec3 (XYZ)
		GL_FLOAT,										// type
		GL_FALSE,										// normalized?
		sizeof(GLfloat) * 8,							// stride (the length of each element in mesh buffer array is 8)
		(void*)(sizeof(GLfloat) * 3)					// array buffer offset (we use 3rd, 4th, 5th)
	);

	// Setting texture coordinates data
	glVertexAttribPointer(
		textureVec2ShaderVariableAttrib,				// texture coordinates shader variable
		2,												// size of vec2 (UV)
		GL_FLOAT,										// type
		GL_FALSE,										// normalized?
		sizeof(GLfloat) * 8,							// stride (the length of each element in mesh buffer array is 8)
		(void*)(sizeof(GLfloat) * 6)					// array buffer offset (we use 6th and 7th)
	);


	glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
	glDrawArrays(GL_TRIANGLES, 0, verticesCount);

	glDisableVertexAttribArray(vertexVec3ShaderVariableAttrib);
	glDisableVertexAttribArray(normalVec3ShaderVariableAttrib);
	glDisableVertexAttribArray(textureVec2ShaderVariableAttrib);
}

MeshBuffer::~MeshBuffer()
{
	glDeleteBuffers(1, &vertexBufferObject);
	glDeleteVertexArrays(1, &VertexArrayID);
	delete [] buffer;
}

} /* namespace cubex */
