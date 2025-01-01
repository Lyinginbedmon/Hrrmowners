package com.lying.data;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public interface ReloadListener<T> extends ResourceReloader
{
	public CompletableFuture<T> load(ResourceManager manager, Profiler profiler, Executor executor);
	
	public CompletableFuture<Void> apply(T data, ResourceManager manager, Profiler profiler, Executor executor);
	
	public default CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepare, Profiler apply, Executor prepExecute, Executor applyExecute)
	{
		CompletableFuture<T> load = load(manager, prepare, prepExecute);
		return load.thenCompose(synchronizer::whenPrepared).thenCompose(t -> apply(t, manager, apply, applyExecute));
	}
	
	public default String getName() { return getId().toString(); }
	
	public Identifier getId();
}
