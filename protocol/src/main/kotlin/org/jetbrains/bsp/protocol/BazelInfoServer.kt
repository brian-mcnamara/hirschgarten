package org.jetbrains.bsp.protocol

import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import java.util.concurrent.CompletableFuture

interface BazelInfoServer {
  @JsonRequest("bazel/info")
  public fun bazelInfo(): CompletableFuture<BazelInfo>
}
