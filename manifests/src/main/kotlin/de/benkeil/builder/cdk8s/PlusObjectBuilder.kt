package de.benkeil.builder.cdk8s

import org.cdk8s.plus32.Resource
import software.constructs.Construct

abstract class PlusObjectBuilder<
    ApiObjectType : Resource,
    BuilderType : software.amazon.jsii.Builder<ApiObjectType>,
    SelfType : PlusObjectBuilder<ApiObjectType, BuilderType, SelfType>,
>(
    internal val scope: Construct,
    id: String,
    // fqn: String,
    builderCreator: (Construct, String) -> BuilderType,
) {
  companion object {}

  private val preBuildActions: MutableCollection<(SelfType) -> Unit> = mutableListOf()
  private val postBuildActions: MutableCollection<(ApiObjectType) -> Unit> = mutableListOf()
  internal val computedId = createId(id)
  internal val builder = builderCreator(scope, computedId)

  @Suppress("UNCHECKED_CAST")
  fun applyBuilder(block: BuilderType.() -> Unit): SelfType {
    builder.apply(block)
    return this as SelfType
  }

  @Suppress("UNCHECKED_CAST")
  fun addPreBuildAction(action: SelfType.() -> Unit): SelfType {
    preBuildActions.add(action)
    return this as SelfType
  }

  @Suppress("UNCHECKED_CAST")
  fun addPostBuildAction(action: (ApiObjectType) -> Unit): SelfType {
    postBuildActions.add(action)
    return this as SelfType
  }

  // private fun createId(type: GroupVersionKind, givenId: String) =
  //     "${type.kind}_${type.apiVersion}_$givenId"
  private fun createId(givenId: String) = givenId

  fun build(): ApiObjectType {
    preBuildActions.forEach { @Suppress("UNCHECKED_CAST") it(this as SelfType) }
    val resource = builder.build()
    postBuildActions.forEach { it(resource) }
    return resource
  }
}
